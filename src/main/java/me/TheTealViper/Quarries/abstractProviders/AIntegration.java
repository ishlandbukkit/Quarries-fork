package me.TheTealViper.Quarries.abstractProviders;

public abstract class AIntegration {

    protected AIntegration() {
        preRegister();
        registerInterface();
        postRegister();
    }

    protected abstract void preRegister();

    protected abstract void postRegister();

    protected abstract void registerInterface();

}
