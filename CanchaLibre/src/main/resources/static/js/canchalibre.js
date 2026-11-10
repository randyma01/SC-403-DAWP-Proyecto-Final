/* CanchaLibre - comportamiento del lado del cliente (sin dependencias).
   Cada bloque se activa solo si encuentra su elemento en la pagina. */
(function () {
    'use strict';
    var MSG = window.CL_MSG || {};

    function moneda(n, simbolo) {
        return (simbolo || '₡') + Math.round(n).toLocaleString('en-US');
    }
    function ready(fn) {
        if (document.readyState !== 'loading') { fn(); } else { document.addEventListener('DOMContentLoaded', fn); }
    }

    ready(function () {

        /* ---------- Confirmaciones de formularios ---------- */
        document.querySelectorAll('form[data-confirmar]').forEach(function (f) {
            f.addEventListener('submit', function (e) {
                if (!window.confirm(MSG.cancelar || '?')) { e.preventDefault(); }
            });
        });

        /* ---------- Filas clicables (panel admin) ---------- */
        document.querySelectorAll('tr[data-href]').forEach(function (tr) {
            tr.addEventListener('click', function () { window.location.href = tr.getAttribute('data-href'); });
        });

        /* ---------- HU-02: filtros del catalogo sin recargar la pagina ---------- */
        var filtros = document.getElementById('filtros');
        if (filtros && filtros.getAttribute('data-url')) {
            var url = filtros.getAttribute('data-url');
            var refrescar = function (pagina) {
                var datos = new URLSearchParams(new FormData(filtros));
                // Parametros vacios no aportan nada al servidor
                Array.from(datos.keys()).forEach(function (k) {
                    if (datos.getAll(k).every(function (v) { return v === ''; })) { datos.delete(k); }
                });
                if (pagina) { datos.set('pagina', pagina); }
                var cont = document.getElementById('resultados');
                if (cont) { cont.style.opacity = '.5'; }
                fetch(url + '?' + datos.toString(), { headers: { 'X-Requested-With': 'fetch' } })
                    .then(function (r) { return r.text(); })
                    .then(function (html) {
                        var actual = document.getElementById('resultados');
                        var tmp = document.createElement('div');
                        tmp.innerHTML = html.trim();
                        var nuevo = tmp.firstElementChild;
                        if (actual && nuevo) { actual.replaceWith(nuevo); }
                        if (window.history && history.replaceState) {
                            var qs = datos.toString();
                            history.replaceState(null, '', qs ? ('?' + qs) : window.location.pathname);
                        }
                    })
                    .catch(function () { filtros.submit(); });
            };
            filtros.querySelectorAll('[data-auto]').forEach(function (el) {
                el.addEventListener('change', function () { refrescar(); });
            });
            document.querySelectorAll('[data-auto][form="filtros"]').forEach(function (el) {
                el.addEventListener('change', function () { refrescar(); });
            });
            filtros.addEventListener('submit', function (e) { e.preventDefault(); refrescar(); });
            // El boton "Buscar" esta fuera del formulario (atributo form): tambien dispara submit
            document.addEventListener('click', function (e) {
                var a = e.target.closest('[data-pagina]');
                if (a) {
                    e.preventDefault();
                    if (!a.parentElement.classList.contains('disabled')) {
                        refrescar(a.getAttribute('data-pagina'));
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    }
                }
            });
            var sync = document.querySelector('[data-sync-disciplina]');
            if (sync) {
                sync.addEventListener('change', function () {
                    filtros.querySelectorAll('input[name="disciplina"]').forEach(function (c) {
                        c.checked = (sync.value !== '' && c.value === sync.value);
                    });
                    refrescar();
                });
            }
        }

        /* ---------- HU-03: galeria de imagenes ---------- */
        var galeria = document.getElementById('galeria');
        if (galeria) {
            var imgs = Array.from(document.querySelectorAll('.cl-miniaturas img'));
            var principal = document.getElementById('galeriaImg');
            var idx = 0;
            var mostrar = function (i) {
                if (!imgs.length) { return; }
                idx = (i + imgs.length) % imgs.length;
                principal.src = imgs[idx].src;
                imgs.forEach(function (m, k) { m.classList.toggle('activa', k === idx); });
            };
            imgs.forEach(function (m, k) { m.addEventListener('click', function () { mostrar(k); }); });
            galeria.querySelectorAll('[data-galeria]').forEach(function (b) {
                b.addEventListener('click', function () { mostrar(idx + parseInt(b.getAttribute('data-galeria'), 10)); });
            });
        }

        /* ---------- HU-04: seleccion de horario ---------- */
        var selEspacio = document.getElementById('selEspacio');
        if (selEspacio) {
            selEspacio.addEventListener('change', function () {
                var u = selEspacio.getAttribute('data-base') + '?espacio=' + encodeURIComponent(selEspacio.value);
                var f = selEspacio.getAttribute('data-fecha');
                if (f) { u += '&fecha=' + encodeURIComponent(f); }
                window.location.href = u;
            });
        }
        var grid = document.getElementById('gridDisp');
        if (grid) {
            var tarifa = document.getElementById('datosTarifa');
            var diurna = parseFloat(tarifa.getAttribute('data-diurna'));
            var nocturna = parseFloat(tarifa.getAttribute('data-nocturna'));
            var boton = document.getElementById('btnContinuar');
            grid.addEventListener('click', function (e) {
                var celda = e.target.closest('.cl-celda.DISPONIBLE');
                if (!celda || celda.disabled) { return; }
                grid.querySelectorAll('.cl-celda.elegida').forEach(function (c) { c.classList.remove('elegida'); });
                celda.classList.add('elegida');
                document.getElementById('selHorario').textContent = celda.getAttribute('data-rango');
                document.getElementById('selHoraInput').value = celda.getAttribute('data-hora');
                var precio = celda.getAttribute('data-nocturno') === 'true' ? nocturna : diurna;
                document.getElementById('selPrecio').textContent = moneda(precio);
                boton.disabled = false;
            });
        }

        /* ---------- HU-05/06/07: cotizacion en vivo de insumos ---------- */
        var formReserva = document.getElementById('formReserva');
        if (formReserva) {
            var pista = parseFloat(formReserva.getAttribute('data-pista'));
            var iva = parseFloat(formReserva.getAttribute('data-iva'));
            var simbolo = formReserva.getAttribute('data-simbolo');
            var tarjetas = Array.from(formReserva.querySelectorAll('.cl-insumo[data-id]'));

            var recalcular = function () {
                var total = pista;
                var cont = document.getElementById('lineasInsumos');
                cont.innerHTML = '';
                tarjetas.forEach(function (t) {
                    var inp = t.querySelector('[data-cant]');
                    if (!inp) { return; }
                    var c = parseInt(inp.value, 10) || 0;
                    t.classList.toggle('activo', c > 0);
                    if (c > 0) {
                        var sub = c * parseFloat(t.getAttribute('data-precio'));
                        total += sub;
                        var linea = document.createElement('div');
                        linea.className = 'cl-linea';
                        var a = document.createElement('span');
                        a.textContent = c + ' x ' + t.getAttribute('data-nombre');   // textContent: sin inyeccion de HTML
                        var b = document.createElement('b');
                        b.textContent = moneda(sub, simbolo);
                        linea.appendChild(a); linea.appendChild(b);
                        cont.appendChild(linea);
                    }
                });
                var ivaIncl = total - total / (1 + iva / 100);
                document.getElementById('resTotal').textContent = moneda(total, simbolo);
                document.getElementById('resIva').textContent = moneda(ivaIncl, simbolo);
            };

            tarjetas.forEach(function (t) {
                var inp = t.querySelector('input[type=number][data-cant]');
                var max = parseInt(t.getAttribute('data-max'), 10) || 0;
                if (inp) {
                    var poner = function (v) {
                        v = Math.max(0, Math.min(max, isNaN(v) ? 0 : v));
                        inp.value = v;
                        recalcular();
                    };
                    t.querySelector('[data-menos]').addEventListener('click', function () { poner((parseInt(inp.value, 10) || 0) - 1); });
                    t.querySelector('[data-mas]').addEventListener('click', function () { poner((parseInt(inp.value, 10) || 0) + 1); });
                    inp.addEventListener('input', function () { poner(parseInt(inp.value, 10)); });
                }
                var sw = t.querySelector('[data-toggle-servicio]');
                if (sw) {
                    var oculto = t.querySelector('input[type=hidden][data-cant]');
                    sw.addEventListener('change', function () { oculto.value = sw.checked ? '1' : '0'; recalcular(); });
                }
            });
            recalcular();
        }

        /* ---------- HU-09: temporizador de pago ---------- */
        var timer = document.getElementById('timer');
        if (timer) {
            var restante = parseInt(timer.getAttribute('data-restante'), 10);
            var total = parseInt(timer.getAttribute('data-total'), 10) || 900;
            var destino = timer.getAttribute('data-destino');
            var reloj = document.getElementById('timerReloj');
            var barra = document.getElementById('timerBarra');
            var fin = Date.now() + restante * 1000;      // se ancla a la hora local para no desfasarse
            var pintar = function () {
                var seg = Math.max(0, Math.round((fin - Date.now()) / 1000));
                var mm = String(Math.floor(seg / 60)).padStart(2, '0');
                var ss = String(seg % 60).padStart(2, '0');
                reloj.textContent = mm + ':' + ss;
                barra.style.width = Math.min(100, (seg / total) * 100) + '%';
                timer.classList.toggle('urgente', seg <= 120);
                if (seg <= 0) {
                    clearInterval(h);
                    window.location.href = destino;   // el servidor marca la reserva como expirada
                }
            };
            var h = setInterval(pintar, 1000);
            pintar();
        }

        /* ---------- Comprobante: arrastrar y soltar ---------- */
        var dz = document.getElementById('dropzone');
        if (dz) {
            var input = document.getElementById('comprobante');
            var nombre = document.getElementById('archivoNombre');
            var mostrarNombre = function () { nombre.textContent = input.files.length ? input.files[0].name : ''; };
            dz.addEventListener('click', function () { input.click(); });
            input.addEventListener('click', function (e) { e.stopPropagation(); });
            input.addEventListener('change', mostrarNombre);
            ['dragenter', 'dragover'].forEach(function (ev) {
                dz.addEventListener(ev, function (e) { e.preventDefault(); dz.classList.add('arrastrando'); });
            });
            ['dragleave', 'drop'].forEach(function (ev) {
                dz.addEventListener(ev, function (e) { e.preventDefault(); dz.classList.remove('arrastrando'); });
            });
            dz.addEventListener('drop', function (e) {
                if (e.dataTransfer && e.dataTransfer.files.length) {
                    input.files = e.dataTransfer.files;
                    mostrarNombre();
                }
            });
        }

        /* ---------- Copiar IBAN ---------- */
        document.querySelectorAll('[data-copiar]').forEach(function (b) {
            b.addEventListener('click', function () {
                var el = document.querySelector(b.getAttribute('data-copiar'));
                if (!el) { return; }
                var texto = el.textContent.trim();
                var ok = function () {
                    var original = b.innerHTML;
                    b.textContent = MSG.copiado || 'OK';
                    setTimeout(function () { b.innerHTML = original; }, 1500);
                };
                if (navigator.clipboard && navigator.clipboard.writeText) {
                    navigator.clipboard.writeText(texto).then(ok);
                } else {
                    var ta = document.createElement('textarea');
                    ta.value = texto; document.body.appendChild(ta); ta.select();
                    try { document.execCommand('copy'); ok(); } catch (err) { /* sin soporte */ }
                    document.body.removeChild(ta);
                }
            });
        });
    });
})();
