package br.com.safefield.domain;

import jakarta.persistence.*;

@Entity
@DiscriminatorValue("SUPERVISOR")
public class SupervisorSeguranca extends Usuario {

    protected SupervisorSeguranca() {}

    public SupervisorSeguranca(String nome, String matricula, String email, String senhaHash) {
        super(nome, matricula, email, senhaHash);
    }

    @Override
    public Perfil getPerfil() { return Perfil.SUPERVISOR; }
}
