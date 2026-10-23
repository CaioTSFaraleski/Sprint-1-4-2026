package br.com.safefield.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("GESTOR")
public class GestorIndustrial extends Usuario {

    private String unidadeIndustrial;

    protected GestorIndustrial() {}

    public GestorIndustrial(String nome, String matricula, String email, String senhaHash, String unidadeIndustrial) {
        super(nome, matricula, email, senhaHash);
        this.unidadeIndustrial = unidadeIndustrial;
    }

    @Override
    public Perfil getPerfil() { return Perfil.GESTOR; }

    public String getUnidadeIndustrial() { return unidadeIndustrial; }
}
