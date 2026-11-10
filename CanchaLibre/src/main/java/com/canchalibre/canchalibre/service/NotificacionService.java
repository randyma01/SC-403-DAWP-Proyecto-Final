package com.canchalibre.canchalibre.service;

import com.canchalibre.canchalibre.domain.Notificacion;
import com.canchalibre.canchalibre.domain.Reserva;
import com.canchalibre.canchalibre.domain.Usuario;
import com.canchalibre.canchalibre.repository.NotificacionRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificacionService {

    private final NotificacionRepository repo;

    public NotificacionService(NotificacionRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void notificar(Usuario usuario, Reserva reserva, String titulo, String mensaje) {
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setReserva(reserva);
        n.setTitulo(titulo);
        n.setMensaje(mensaje.length() > 500 ? mensaje.substring(0, 500) : mensaje);
        repo.save(n);
    }

    @Transactional(readOnly = true)
    public long noLeidas(Long usuarioId) {
        return repo.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> listar(Long usuarioId) {
        return repo.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
    }

    @Transactional
    public void marcarTodasLeidas(Long usuarioId) {
        List<Notificacion> lista = repo.findByUsuarioIdOrderByCreadoEnDesc(usuarioId);
        for (Notificacion n : lista) {
            n.setLeida(true);
        }
    }
}
