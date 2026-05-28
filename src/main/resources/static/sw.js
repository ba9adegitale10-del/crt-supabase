// CRT Megrine Service Worker
const CACHE_NAME = 'crt-megrine-v1';
const OFFLINE_URL = '/offline';

// Fichiers à mettre en cache
const STATIC_ASSETS = [
  '/',
  '/dashboard',
  '/css/main.css',
  '/images/logo-crt.png',
  '/icons/icon-192.png',
  '/icons/icon-512.png',
  '/manifest.json'
];

// Installation
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME).then(cache => {
      return cache.addAll(STATIC_ASSETS);
    })
  );
  self.skipWaiting();
});

// Activation
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(keys =>
      Promise.all(
        keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key))
      )
    )
  );
  self.clients.claim();
});

// Fetch - Network First, fallback to cache
self.addEventListener('fetch', event => {
  // Skip non-GET and non-HTTP requests
  if (event.request.method !== 'GET') return;
  if (!event.request.url.startsWith('http')) return;

  event.respondWith(
    fetch(event.request)
      .then(response => {
        // Cache les ressources statiques
        if (event.request.url.includes('/css/') ||
            event.request.url.includes('/images/') ||
            event.request.url.includes('/icons/')) {
          const clone = response.clone();
          caches.open(CACHE_NAME).then(cache => cache.put(event.request, clone));
        }
        return response;
      })
      .catch(() => {
        // Offline - retourner depuis le cache
        return caches.match(event.request).then(cached => {
          if (cached) return cached;
          // Page offline
          return new Response(
            `<!DOCTYPE html>
<html lang="fr">
<head>
<meta charset="UTF-8"/>
<meta name="viewport" content="width=device-width, initial-scale=1.0"/>
<title>Hors ligne — CRT Megrine</title>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { font-family: sans-serif; min-height:100vh; display:flex; align-items:center; justify-content:center; background:#CC1A1A; }
  .box { background:#fff; border-radius:20px; padding:40px 32px; text-align:center; max-width:320px; margin:20px; }
  img { width:80px; height:80px; margin-bottom:20px; border-radius:16px; }
  h1 { font-size:20px; color:#1A0A0A; margin-bottom:8px; }
  p { font-size:14px; color:#8A7A7A; line-height:1.6; margin-bottom:20px; }
  button { background:#CC1A1A; color:#fff; border:none; padding:12px 24px; border-radius:10px; font-size:14px; font-weight:600; cursor:pointer; }
</style>
</head>
<body>
<div class="box">
  <img src="/icons/icon-192.png" alt="CRT"/>
  <h1>Pas de connexion</h1>
  <p>Vous êtes hors ligne. Vérifiez votre connexion internet et réessayez.</p>
  <button onclick="window.location.reload()">Réessayer</button>
</div>
</body>
</html>`,
            { headers: { 'Content-Type': 'text/html; charset=utf-8' } }
          );
        });
      })
  );
});
