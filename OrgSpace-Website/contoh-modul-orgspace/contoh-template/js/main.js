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
