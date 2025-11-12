package com.pos.models;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

// Aceasta clasa reprezinta cheia primara compusa pentru tabela JOIN_PE
@Embeddable
public class PachetEvenimentId implements Serializable {

    private int pachetId;
    private int evenimentId;

    // Constructor, gettere, settere, hashCode și equals sunt necesare
    public PachetEvenimentId() {}

    public PachetEvenimentId(int pachetId, int evenimentId) {
        this.pachetId = pachetId;
        this.evenimentId = evenimentId;
    }

    // Gettere și Settere
    public int getPachetId() { return pachetId; }
    public void setPachetId(int pachetId) { this.pachetId = pachetId; }
    public int getEvenimentId() { return evenimentId; }
    public void setEvenimentId(int evenimentId) { this.evenimentId = evenimentId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PachetEvenimentId that = (PachetEvenimentId) o;
        return pachetId == that.pachetId && evenimentId == that.evenimentId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pachetId, evenimentId);
    }
}
