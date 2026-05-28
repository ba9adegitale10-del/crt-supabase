package com.example.megrine.controller;

import com.example.megrine.model.StockItem;
import com.example.megrine.model.StockMovement;
import com.example.megrine.model.ActivityLog;
import com.example.megrine.repository.StockItemRepository;
import com.example.megrine.repository.StockMovementRepository;
import com.example.megrine.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/stock")
public class StockController {

    @Autowired private StockItemRepository stockRepo;
    @Autowired private StockMovementRepository movementRepo;
    @Autowired private ActivityLogService logService;

    @GetMapping
    public String list(Model model, @RequestParam(required=false) String search,
                       @RequestParam(required=false) String category) {
        java.util.List<StockItem> items;
        if (search != null && !search.isBlank()) {
            items = stockRepo.findByNameContainingIgnoreCase(search); model.addAttribute("search", search);
        } else if (category != null && !category.isBlank()) {
            try { items = stockRepo.findByCategoryEnum(StockItem.StockCategory.valueOf(category)); } catch (Exception e) { items = stockRepo.findAll(); }
            model.addAttribute("selectedCategory", category);
        } else { items = stockRepo.findAll(); }
        model.addAttribute("items", items);
        model.addAttribute("categories", StockItem.StockCategory.values());
        model.addAttribute("lowStockCount", stockRepo.findLowStock().size());
        model.addAttribute("recentMovements", movementRepo.findTop20ByOrderByMovedAtDesc());
        return "stock/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        StockItem item = new StockItem(); item.setCategoryEnum(StockItem.StockCategory.OTHER);
        item.setQuantity(0); item.setQuantityMin(0);
        model.addAttribute("item", item); return "stock/form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(value="id",required=false) Long id,
            @RequestParam("name") String name,
            @RequestParam(value="categoryEnum",defaultValue="OTHER") String category,
            @RequestParam(value="unit",required=false) String unit,
            @RequestParam(value="quantity",defaultValue="0") Integer quantity,
            @RequestParam(value="quantityMin",defaultValue="0") Integer quantityMin,
            @RequestParam(value="description",required=false) String description,
            @RequestParam(value="location",required=false) String location,
            RedirectAttributes ra) {
        boolean isNew = (id == null);
        StockItem item = isNew ? new StockItem() : stockRepo.findById(id).orElse(new StockItem());
        Integer oldQty = item.getQuantity() != null ? item.getQuantity() : 0;
        item.setName(name); item.setUnit(unit); item.setQuantity(quantity);
        item.setQuantityMin(quantityMin); item.setDescription(description);
        item.setLocation(location); item.setLastUpdated(LocalDate.now());
        try { item.setCategoryEnum(StockItem.StockCategory.valueOf(category)); } catch (Exception e) { item.setCategoryEnum(StockItem.StockCategory.OTHER); }
        stockRepo.save(item);
        String details = "Qte: " + quantity + " " + unit + " | Min: " + quantityMin + " | Lieu: " + location + " | Cat: " + category;
        if (isNew) logService.log("Ajout article stock: " + name, ActivityLog.ActionType.CREATE, "Stock", name, details);
        else {
            logService.log("Modification article: " + name, ActivityLog.ActionType.UPDATE, "Stock", name, details);
            if (!oldQty.equals(quantity)) {
                int diff = quantity - oldQty;
                StockMovement mv = new StockMovement();
                mv.setStockItem(item); mv.setQuantity(Math.abs(diff));
                mv.setType(diff > 0 ? StockMovement.MovementType.IN : StockMovement.MovementType.OUT);
                mv.setReason("Modification manuelle"); mv.setMovedAt(LocalDateTime.now());
                movementRepo.save(mv);
                logService.log((diff>0?"Entree":"Sortie") + " stock: " + name, diff>0?ActivityLog.ActionType.CREATE:ActivityLog.ActionType.UPDATE,
                    "Mouvement Stock", name, "Quantite: " + Math.abs(diff) + " " + unit + " | Ancien: " + oldQty + " | Nouveau: " + quantity);
            }
        }
        ra.addFlashAttribute("success", "Article enregistre !");
        return "redirect:/stock";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) { model.addAttribute("item", stockRepo.findById(id).orElseThrow()); return "stock/form"; }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            StockItem item = stockRepo.findById(id).orElseThrow();
            String name = item.getName();
            java.util.List<StockMovement> movements = movementRepo.findByStockItemIdOrderByMovedAtDesc(id);
            movementRepo.deleteAll(movements);
            stockRepo.deleteById(id);
            logService.log("Suppression article stock: " + name, ActivityLog.ActionType.DELETE, "Stock", name, movements.size() + " mouvements supprimes");
            ra.addFlashAttribute("success", "Article supprime.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Impossible de supprimer."); }
        return "redirect:/stock";
    }

    @PostMapping("/move")
    public String move(@RequestParam("itemId") Long itemId,
            @RequestParam("moveType") String moveType,
            @RequestParam("moveQty") Integer qty,
            @RequestParam(value="reason",required=false) String reason,
            Authentication auth, RedirectAttributes ra) {
        try {
            StockItem item = stockRepo.findById(itemId).orElseThrow();
            boolean isIn = "IN".equals(moveType);
            int oldQty = item.getQuantity();
            int newQty = isIn ? oldQty + qty : Math.max(0, oldQty - qty);
            item.setQuantity(newQty); item.setLastUpdated(LocalDate.now());
            stockRepo.save(item);
            StockMovement mv = new StockMovement();
            mv.setStockItem(item); mv.setQuantity(qty);
            mv.setType(isIn ? StockMovement.MovementType.IN : StockMovement.MovementType.OUT);
            mv.setReason(reason != null && !reason.isBlank() ? reason : (isIn ? "Entree stock" : "Sortie stock"));
            mv.setPerformedBy(auth != null ? auth.getName() : "Systeme");
            mv.setMovedAt(LocalDateTime.now());
            movementRepo.save(mv);
            logService.log((isIn?"Entree":"Sortie") + " stock: " + item.getName(),
                isIn ? ActivityLog.ActionType.CREATE : ActivityLog.ActionType.UPDATE,
                "Mouvement Stock", item.getName(),
                "Qte: " + qty + " | Avant: " + oldQty + " | Apres: " + newQty + " | Raison: " + mv.getReason());
            ra.addFlashAttribute("success", (isIn?"Entree":"Sortie") + " de " + qty + " enregistree.");
        } catch (Exception e) { ra.addFlashAttribute("error", "Erreur mouvement stock."); }
        return "redirect:/stock";
    }
}
