# Injection dans JZSInit.install()

Une seule ligne à ajouter dans `JZSInit.smali`, juste après l'appel à
`JZSModMenu->attach` (dans le même bloc `:try`) :

```smali
    invoke-static {p0}, Lcom/jzs/brawl/JZSModMenu;->attach(Landroid/app/Activity;)V

    invoke-static {p0}, Lcom/jzs/brawl/JZSRemote;->init(Landroid/app/Activity;)V
```

`p0` est l'Activity. Aucun registre supplémentaire n'est nécessaire
(`.locals` reste inchangé). Le script `build/add_remote.sh` fait cette
injection automatiquement.
