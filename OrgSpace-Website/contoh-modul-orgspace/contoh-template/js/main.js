// Menu HP: tombol "Menu" membuka/menutup navigasi. Event dipasang di sini, bukan dengan onclick di HTML
// (atribut on... dihapus saat website diterbitkan).
var toggle = document.getElementById("menu-toggle");
var menu = document.getElementById("menu");
toggle.addEventListener("click", function () {
  var open = menu.classList.toggle("open");
  toggle.setAttribute("aria-expanded", open ? "true" : "false");
});
menu.querySelectorAll("a").forEach(function (link) {
  link.addEventListener("click", function () {
    menu.classList.remove("open");
    toggle.setAttribute("aria-expanded", "false");
  });
});

// Filter prestasi per tingkat, dibuat dari data modul "prestasi". Kartu dan data baru tersedia setelah OrgSpace
// mengisi modul, jadi tunggu event "orgspace:siap" (atau langsung jalan jika sudah siap).
function buatFilterPrestasi() {
  var data = window.orgspace.data.collections.prestasi || [];
  var tingkat = [];
  data.forEach(function (p) {
    if (p.tingkat && tingkat.indexOf(p.tingkat) < 0) tingkat.push(p.tingkat);
  });
  var wadah = document.getElementById("filter-prestasi");
  if (tingkat.length < 2) return;
  ["Semua"].concat(tingkat).forEach(function (t, i) {
    var tombol = document.createElement("button");
    tombol.type = "button";
    tombol.className = i === 0 ? "chip aktif" : "chip";
    tombol.textContent = t;
    tombol.addEventListener("click", function () {
      wadah.querySelectorAll(".chip").forEach(function (c) { c.classList.remove("aktif"); });
      tombol.classList.add("aktif");
      document.querySelectorAll("#prestasi [data-ws-item]").forEach(function (kartu) {
        var nilai = kartu.querySelector('[data-ws-field="tingkat"]').textContent;
        kartu.style.display = t === "Semua" || nilai === t ? "" : "none";
      });
    });
    wadah.appendChild(tombol);
  });
  wadah.hidden = false;
}
if (window.orgspace && window.orgspace.siap) buatFilterPrestasi();
else document.addEventListener("orgspace:siap", buatFilterPrestasi);
