/**
 * Script yang disisipkan ke setiap halaman template. Ditulis sebagai teks JavaScript biasa (bukan kode TypeScript
 * yang di-bundle) karena dijalankan di dalam halaman template, di luar React.
 */

/**
 * Disisipkan paling awal di <head>, sebelum script template. Halaman sandbox tidak boleh memakai
 * localStorage/sessionStorage/cookie (browser melempar SecurityError), sehingga script template yang memakainya
 * (mis. tombol dark mode) akan error. Penggantinya menyimpan di memori: tetap jalan, tapi hilang saat halaman ditutup.
 */
export const STORAGE_SHIM = `(function(){
  function memory(){var s={};return{
    getItem:function(k){return Object.prototype.hasOwnProperty.call(s,k)?s[k]:null},
    setItem:function(k,v){s[k]=String(v)},removeItem:function(k){delete s[k]},clear:function(){s={}},
    key:function(i){return Object.keys(s)[i]||null},get length(){return Object.keys(s).length}}}
  ["localStorage","sessionStorage"].forEach(function(n){
    try{window[n].getItem("x")}catch(e){try{Object.defineProperty(window,n,{value:memory(),configurable:true})}catch(_){}}
  });
  try{document.cookie}catch(e){try{Object.defineProperty(document,"cookie",{get:function(){return""},set:function(){},configurable:true})}catch(_){}}
})();`;

/**
 * Disisipkan sebelum </body>. Mengisi modul data-ws-* dari data yang ditanam server di #orgspace-data, lalu
 * mengirim event "orgspace:siap" dan membuka window.orgspace = { siap, data: { slug, news, collections, stats, org } } untuk
 * script provider. Juga mengirim form data-ws-form. Halaman publik selalu tampil sebagai tamu.
 */
export const MODULE_RUNTIME = `(function(){
  var el=document.getElementById("orgspace-data"); if(!el) return;
  var d=JSON.parse(el.textContent);
  var NEWS=["berita","kegiatan","pengumuman"];
  function safeHref(v){return /^(https?:|mailto:|tel:|\\/|#)/i.test(v)}
  function safeSrc(v){return /^(https?:|\\/)/i.test(v)}
  function fill(card,item){
    card.querySelectorAll("[data-ws-field]").forEach(function(f){
      var v=item[f.getAttribute("data-ws-field")]; if(v===undefined||v===null||v==="") return; v=String(v);
      var t=(f.getAttribute("data-ws-type")||"").toLowerCase();
      // Portal menyimpan tanggal sebagai 2026-10-19; pengunjung melihat "19 Oktober 2026".
      if(t==="tanggal"&&/^\\d{4}-\\d{2}-\\d{2}$/.test(v)){ var dt=new Date(v+"T00:00:00");
        if(!isNaN(dt)) v=dt.toLocaleDateString("id-ID",{day:"numeric",month:"long",year:"numeric"}); }
      if(t==="paragraf") f.style.whiteSpace="pre-line";
      if(f.tagName==="IMG"){ if(safeSrc(v)) f.setAttribute("src",v); } else { f.textContent=v; }
    });
    card.querySelectorAll("[data-ws-link]").forEach(function(a){
      var v=item[a.getAttribute("data-ws-link")]; v=v===undefined||v===null?"":String(v).trim();
      if(v&&safeHref(v)) a.setAttribute("href",v);
    });
  }
  document.querySelectorAll("[data-ws-list],[data-ws-collection]").forEach(function(box){
    var name=(box.getAttribute("data-ws-list")||box.getAttribute("data-ws-collection")||"").toLowerCase();
    var items=NEWS.indexOf(name)>=0?d.news:(d.collections[name]||[]);
    var limit=parseInt(box.getAttribute("data-ws-limit")||"",10); if(limit>0) items=items.slice(0,limit);
    var card=box.querySelector("[data-ws-item]"), empty=box.querySelector("[data-ws-empty]");
    if(!card) return;
    if(!items.length){ card.style.display="none"; if(empty) empty.style.display=""; return; }
    if(empty) empty.style.display="none";
    var blank=card.cloneNode(true), last=card;
    items.forEach(function(item,i){
      var c=i===0?card:blank.cloneNode(true); fill(c,item);
      if(i>0){ last.parentNode.insertBefore(c,last.nextSibling); last=c; }
    });
  });
  // Angka ringkasan & profil organisasi. Profil yang belum diisi disembunyikan agar teks contoh template (mis.
  // "Alamat organisasi") tidak tampil di website asli; logo tetap memakai logo template.
  document.querySelectorAll("[data-ws-stat]").forEach(function(x){
    var v=d.stats[(x.getAttribute("data-ws-stat")||"").toLowerCase()];
    x.textContent=String(v===undefined?0:v);
  });
  document.querySelectorAll("[data-ws-org]").forEach(function(x){
    var k=(x.getAttribute("data-ws-org")||"").toLowerCase(), v=d.org[k];
    if(!v){ if(k!=="logo") x.style.display="none"; return; }
    if(x.tagName==="IMG"){ if(safeSrc(v)) x.setAttribute("src",v); }
    else if(x.tagName==="A"&&(k==="email"||k==="whatsapp"||k==="instagram")){
      x.textContent=v;
      x.setAttribute("href",k==="email"?"mailto:"+v:k==="whatsapp"?"https://wa.me/"+v.replace(/\\D/g,"").replace(/^0/,"62"):"https://instagram.com/"+v.replace(/^@/,""));
    } else x.textContent=v;
  });
  // Script provider berjalan sebelum script ini, jadi diberi tahu lewat event setelah modul terisi.
  // Data modul juga dibuka (salinan) agar provider bisa membuat filter, grafik, atau peta sendiri.
  window.orgspace={siap:true,data:JSON.parse(el.textContent)};
  document.dispatchEvent(new CustomEvent("orgspace:siap",{detail:window.orgspace.data}));
  document.querySelectorAll('a[data-ws-auth="login"]').forEach(function(a){a.setAttribute("href","/"+d.slug+"/admin")});
  document.querySelectorAll('[data-ws-auth="logout"],[data-ws-role]').forEach(function(x){x.style.display="none"});
  function notice(form,ok,text){
    var box=form.querySelector("[data-ws-form-alert]");
    if(!box){ box=document.createElement("div"); box.setAttribute("role","status");
      box.style.cssText="position:fixed;left:16px;right:16px;bottom:16px;z-index:2147483647;padding:12px 16px;border-radius:10px;font:14px/1.4 system-ui,sans-serif;box-shadow:0 6px 24px rgba(0,0,0,.2)";
      document.body.appendChild(box); setTimeout(function(){box.remove()},4000); }
    box.style.display="block"; box.style.background=ok?"#dcfce7":"#fee2e2"; box.style.color=ok?"#166534":"#991b1b";
    box.textContent=text;
  }
  document.querySelectorAll("form[data-ws-form],[data-ws-form] form").forEach(function(form){
    form.addEventListener("submit",function(e){
      e.preventDefault();
      if(d.preview){ notice(form,true,"Ini preview template: formulir tidak dikirim."); return; }
      var name=form.getAttribute("data-ws-form")||form.closest("[data-ws-form]").getAttribute("data-ws-form");
      var btn=form.querySelector('[type="submit"]'), label=btn?(btn.value||btn.textContent):"";
      if(btn){ btn.disabled=true; if(btn.tagName==="INPUT") btn.value="Mengirim..."; else btn.textContent="Mengirim..."; }
      var data={}; new FormData(form).forEach(function(v,k){ if(typeof v==="string") data[k]=v; });
      fetch("/api/forms/"+encodeURIComponent(d.slug)+"/"+encodeURIComponent(name),{
        method:"POST",headers:{"Content-Type":"application/json"},body:JSON.stringify(data)})
        .then(function(r){return r.json().then(function(j){return {ok:r.ok,j:j}})})
        .then(function(x){ notice(form,x.ok,x.j.message||(x.ok?"Terkirim.":"Gagal mengirim.")); if(x.ok) form.reset(); })
        .catch(function(){ notice(form,false,"Gagal mengirim. Periksa koneksi internet lalu coba lagi."); })
        .then(function(){ if(btn){ btn.disabled=false; if(btn.tagName==="INPUT") btn.value=label; else btn.textContent=label; } });
    });
  });
})();`;
