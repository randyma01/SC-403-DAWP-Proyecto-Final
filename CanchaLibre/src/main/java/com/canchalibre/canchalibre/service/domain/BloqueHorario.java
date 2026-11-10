package com.canchalibre.canchalibre.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Bloque de una hora de un espacio. Solo existe una fila cuando el bloque deja de estar libre.
 * UNIQUE(espacio, fecha, hora_inicio) garantiza que nadie reserve el mismo bloque dos veces.
 */
@Entity
@Table(name = "bloques_horarios")
public class BloqueHorario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "espacio_id")
    private Espacio espacio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoBloque estado = EstadoBloque.DISPONIBLE;

    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @Column(length = 200)
    private String motivo;

    // ---- generado: getters y setters ----
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Espacio getEspacio() {
        return espacio;
    }

    public void setEspacio(Espacio espacio) {
        this.espacio = espacio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public EstadoBloque getEstado() {
        return estado;
    }

    public void setEstado(EstadoBloque estado) {
        this.estado = estado;
    }

    public LocalDateTime getExpiraEn() {
        return expiraEn;
    }

    public void setExpiraEn(LocalDateTime expiraEn) {
        this.expiraEn = expiraEn;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }


    /** true si el bloque esta efectivamente libre en el instante dado (incluye retenciones vencidas). */
    public boolean estaLibre(LocalDateTime ahora) {
        if (estado == EstadoBloque.DISPONIBLE) {
            return true;
        }
        return estado == EstadoBloque.APARTADO_TEMPORAL && expiraEn != null && !expiraEn.isAfter(ahora);
    }
}
