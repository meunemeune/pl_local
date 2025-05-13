import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant le jeu de Monopoly.
 * Elle est responsable de l'initialisation et de la gestion globale d'une partie.
 * Elle contient les joueurs, le plateau et gère les règles et les interactions entre les joueurs.
 */
public class JeuMonopoly {
    /** Le plateau du jeu, contenant les cases */
    private Plateau plateau;
    /** La liste des joueurs participant à la partie */
    private List<Joueur> joueurs;
    /** L'index du joueur actuel dans la liste des joueurs */
    private int joueurActuelIndex;

    /**
     * Constructeur de la classe JeuMonopoly.
     * 
     */
    public JeuMonopoly() {
        this.plateau = new Plateau();
        this.joueurs = new ArrayList<>();
        this.joueurActuelIndex = 0;
    }

    /**
     * Démarre la partie de Monopoly.
     * Cette méthode contient la boucle principale du jeu.
     */
    public void demarrerJeu() {
        System.out.println("La partie démarre !");
        
        // Boucle principale du jeu
        while (!jeuTermine()) {
            Joueur joueurActuel = getJoueurActuel();
            
            // Affichage informations tour
            System.out.println("\n=== Tour de " + joueurActuel.getNom() + " ===");
            System.out.println("Argent : " + joueurActuel.getSolde() + "€");
            System.out.println("Position : " + joueurActuel.getPosition());
            
            // Vérifier si le joueur est en banqueroute
            if (!joueurActuel.estActif()) {
                System.out.println(joueurActuel.getNom() + " est non actif, il ne joue pas.");
                joueurSuivant();
                continue;
            }
            
            // Gestion du tour de jeu
            faireJouer(joueurActuel);
            
            // Affichage état final du joueur
            System.out.println(joueurActuel.getNom() + " possède à la fin de son tour :");
            System.out.println("- Argent : " + joueurActuel.getSolde() + "€");
            if (!joueurActuel.getProprietes().isEmpty()) {
                System.out.println("- Propriétés :");
                for (CasesPropriete propriete : joueurActuel.getProprietes()) {
                    System.out.println("  * " + propriete.getNom());
                }
            }
            
            // Passage au joueur suivant
            joueurSuivant();
            
            // pause entre les tours
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        // Fin partie
        Joueur gagnant = null;
        for (Joueur joueur : joueurs) {
            if (joueur.estActif()) {
                gagnant = joueur;
                break;
            }
        }
        
        if (gagnant != null) {
            System.out.println("\n=== Fin de la partie ===");
            System.out.println("Le vainqueur est " + gagnant.getNom() + " !");
        } else {
            System.out.println("\n=== Fin de la partie ===");
            System.out.println("Tous les joueurs sont en banqueroute !");
        }
    }

    /**
     * Ajoute un joueur au jeu.
     * @param nom Le nom du joueur
     * @param iconeId Une icône ou identifiant visuel associé au joueur
     */
    public void ajouterJoueur(String nom, int iconeId) {
      // solde initial 
        Joueur joueur = new Joueur(nom, 1500);
        joueur.setPosition(0); 
        joueurs.add(joueur);
    }

    /**
     * Vérifie si la partie est terminée.
     * Affiche le nom du gagnant si un seul joueur reste actif.
     * @return true si un seul joueur ou aucun joueur reste actif, sinon false
     */
    public boolean jeuTermine() {
        int joueursActifs = 0;
        Joueur gagnant = null;

        for (Joueur joueur : joueurs) {
            if (joueur.estActif()) {
                joueursActifs++;
                gagnant = joueur;
            }
        }

        if (joueursActifs == 1) {
            System.out.println(" Le gagnant est : " + gagnant.getNom() + " !");
        } else if (joueursActifs == 0) {
            System.out.println("Tous les joueurs ont été éliminés. Pas de gagnant !");
        }

        return joueursActifs <= 1;
    }

    /**
     * Permet à un joueur de jouer son tour
     * @param joueur Le joueur qui doit jouer son tour
     */
    public void faireJouer(Joueur joueur) { 
        if (joueur.estEnPrison()) {
            System.out.println(joueur.getNom() + " est en prison.");
            
            // Options de sortie de prison
            if (joueur.getCartesSortiePrison() > 0) {
                System.out.println(joueur.getNom() + " utilise une carte Sortie de Prison.");
                joueur.utiliserCarteSortiePrison();
                jouerTourNormal(joueur);
                return;
            }
            
            // Lancer les dés pour tenter un double
            int des = lancerDes();
            int de1 = des / 2 + des % 2;
            int de2 = des / 2;
            
            if (de1 == de2) {
                System.out.println(joueur.getNom() + " a fait un double (" + de1 + "," + de2 + ") et sort de prison !");
                joueur.reduireToursEnPrison(); // Mettre à 0 les tours de prison
                jouerUnite(joueur, des);
                Cases caseActuelle = plateau.getCase(joueur.getPosition());
                caseActuelle.surCase(joueur);
            } else {
                System.out.println(joueur.getNom() + " a fait (" + de1 + "," + de2 + ") et reste en prison.");
                joueur.reduireToursEnPrison();
                
                // Si le joueur a épuisé ses 3 tours en prison, il doit payer et sortir
                if (!joueur.estEnPrison()) {
                    System.out.println(joueur.getNom() + " a passé 3 tours en prison, paie 50€ et sort.");
                    joueur.retirerArgent(50); // Frais de sortie de prison après 3 tours
                    jouerTourNormal(joueur);
                }
            }
        } else {
            // Joueur libre
            jouerTourNormal(joueur);
        }
    }
    
    /**
     * Gère un tour normal 
     * @param joueur Le joueur qui joue son tour
     */
    private void jouerTourNormal(Joueur joueur) {
        int des = lancerDes();
        System.out.println(joueur.getNom() + " a lancé un " + des);
        
      
        
        jouerUnite(joueur, des);
        
        Cases caseActuelle = plateau.getCase(joueur.getPosition());
        System.out.println(joueur.getNom() + " arrive sur la case : " + caseActuelle.getNom());
        caseActuelle.surCase(joueur);
        
    }
    
    /**
     * Simule le lancer de dés
     * @return La somme des dés
     */
    public int lancerDes() {
        // Simulation d'un lancer de deux dés
        int de1 = (int) (Math.random() * 6) + 1;
        int de2 = (int) (Math.random() * 6) + 1;
        return de1 + de2;
    }
    
    /**
     * Déplace le joueur du nombre de cases indiqué par les dés
     * @param joueur Le joueur à déplacer
     * @param valeurDes La valeur des dés
     */
    private void jouerUnite(Joueur joueur, int valeurDes) {
        int anciennePosition = joueur.getPosition();
        int nouvellePosition = (anciennePosition + valeurDes) % plateau.getNombreCases();
        
       
        if (nouvellePosition < anciennePosition) {
            // On passe la case départ et on reçoit de l'argent
            CaseGo caseDepart = (CaseGo) plateau.getCase(0);
            int bonus = caseDepart.getbonus();
            joueur.ajouterArgent(bonus);
            System.out.println(joueur.getNom() + " est passé par la case Départ et reçoit " + bonus + "€");
        }
        
        joueur.setPosition(nouvellePosition);
        System.out.println(joueur.getNom() + " se déplace à la case " + nouvellePosition);
    }

    /**
     * Retourne la liste des joueurs du jeu.
     * @return la liste des joueurs
     */
    public List<Joueur> getJoueurs() {
        return joueurs;
    }

    /**
     * Retourne le plateau de jeu.
     * @return le plateau
     */
    public Plateau getPlateau() {
        return plateau;
    }
    
    /**
     * Retourne le joueur dont c'est actuellement le tour.
     * @return le joueur actuel
     */
    public Joueur getJoueurActuel() {
        if (joueurs.isEmpty()) {
            return null;
        }
        return joueurs.get(joueurActuelIndex);
    }
    
    /**
     * Passe au joueur suivant.
     */
    public void joueurSuivant() {
        if (!joueurs.isEmpty()) {
            joueurActuelIndex = (joueurActuelIndex + 1) % joueurs.size();
        }
    }
}
