package donnees;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import modele.Joueur;
import modele.Maladie;
import modele.Partie;
import modele.Ville;
import modele.cartes.*;
import modele.exceptions.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.*;

public class BibDonneesMongoDB {

    private static final String SERVEURPORT = System.getenv("Database_URL");
    private static final String DATABASE = System.getenv("Database");
    private static final String VILLES = "villes";
    private static final String MALADIES = "maladies";

    private static final String PARTIES = "parties";
    private static final String USERS = "users";

    private Map<String, Maladie> mapMaladie;
    private Map<String, Ville> mapVilles;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> villesMongoCollection;
    private MongoCollection<Document> maladiesMongoCollection;
    private MongoCollection<Document> partiesMongoCollection;
    private MongoCollection<Document> usersMongoCollection;
    private MongoCursor<Document> cursorMaladie;
    private MongoCursor<Document> cursorVilles;
    private MongoCursor<Document> cursorUsers;



    public BibDonneesMongoDB() {
        mapMaladie= new HashMap<>();
        mapVilles= new HashMap<>();
        mongoClient = MongoClients.create(SERVEURPORT);
        database = mongoClient.getDatabase(DATABASE);
        villesMongoCollection = database.getCollection(VILLES);
        maladiesMongoCollection = database.getCollection(MALADIES);
        partiesMongoCollection = database.getCollection(PARTIES);
        usersMongoCollection = database.getCollection(USERS);
    }

     /**
     * Initialisation des maladies à partir du document json Maladies
     * @return
     */
    public void initMaladies(){
        if(mapMaladie.isEmpty()){
            try {
                cursorMaladie = maladiesMongoCollection.find().iterator();
                while (cursorMaladie.hasNext()) {
                    Document document = cursorMaladie.next();
                    String couleur = document.get("couleur", String.class);
                    String id= document.get("_id",String.class);
                    mapMaladie.put(couleur, new Maladie(couleur));
                }
            } finally {
                cursorMaladie.close();
            }
        }
    }


    /** Initialisation des villes à partir du document json Villes
     *
     * @return
     */
    public void initVilles() throws VilleVoisineAElleMemeException, VilleDoublonVoisinException {
        if(mapVilles.isEmpty()){
            try {
                cursorVilles = villesMongoCollection.find().iterator();
                while (cursorVilles.hasNext()) {
                    Document document = cursorVilles.next();
                    String id= document.get("_id", String.class);
                    String nom= document.get("nom",String.class);
                    int population= document.get("population",Integer.class);
                    Document document1= (Document) document.get("maladieParDefaut");
                    String idMaladie = document1.get("_id",String.class);
                    Document naladieDocument = maladiesMongoCollection.find(Filters.eq("_id",idMaladie)).first();
                    String couleurMaladie= naladieDocument.get("couleur",String.class);
                    Ville v = new Ville(nom,population,mapMaladie.get(couleurMaladie));
                    mapVilles.put(nom,v);
                }
            } finally {
                cursorVilles.close();
            }
        }
        this.ajouterVillesAlentours();
    }

    public String getIdVilleByName(String ville) {
        Bson filters = Filters.eq("nom", ville);
        Bson projections = Projections.fields(Projections.include("_id"));

        Document result = villesMongoCollection.find(filters).projection(projections).first();

        assert result != null;
        return result.getString("_id");
    }

    public String getIdMaladieByName(String maladie) {
        Bson filters = Filters.eq("couleur", maladie);
        Bson projections = Projections.fields(Projections.include("_id"));

        Document result = maladiesMongoCollection.find(filters).projection(projections).first();

        assert result != null;
        return result.getString("_id");
    }

    public void insertUser(String pseudo,String password){
        Document doc = new Document();
        doc.put("_id", new ObjectId());
        doc.put("pseudo", pseudo);
        doc.put("password", password);
        usersMongoCollection.insertOne(doc);
    }
    public Map<String,String> getUserByPseudo(String pseudo){
        Bson filters = Filters.eq("pseudo", pseudo);
        Bson projections = Projections.fields(Projections.exclude("_id"));

        Document result = usersMongoCollection.find(filters).projection(projections).first();
        if(Objects.nonNull(result)){
            return Map.of("pseudo",result.get("pseudo", String.class),
                    "password",result.get("password", String.class));
        }
        return null;
    }

    public void updatePartie(Partie partie) throws PartieNonDemarreeException, PartieTermineeException {
        // Création d'un document à partir de l'objet "partie"
        Document doc = new Document();
        Document docPlateau = new Document();
        doc.put("_id", "partie:" + partie.getIdPartie());
        doc.put("joueurCourant", partie.getJoueurCourant().getPseudo());
        CarteEvenement cartePlanUrgence = partie.getCarteEntrepPlanificateurUrgence();
        if (!Objects.isNull(cartePlanUrgence)) {
            doc.put("cartePlanificateurUrgence", partie.getCarteEntrepPlanificateurUrgence().getTypeEvenement().name());
        }

        // Création de la liste de joueurs
        List<Document> joueurs = new ArrayList<>();
        for (Joueur joueur : partie.getJoueurs()) {
            // Création d'un document pour chaque joueur
            Document joueurDoc = new Document();
            joueurDoc.put("pseudo", joueur.getPseudo());
            joueurDoc.put("role", joueur.getRole());

            Document emplacementDoc = new Document();
            String ville = joueur.getEmplacement().getNom();
            emplacementDoc.put("_id", getIdVilleByName(ville));
            joueurDoc.put("emplacement", emplacementDoc);

            List<Document> cartesVille = new ArrayList<>();
            List<String> cartesEvenement = new ArrayList<>();
            for (ICarteJoueur carte : joueur.getCartes()) {
                if (carte instanceof CarteVilleJoueur) {
                    Document carteDoc = new Document();
                    carteDoc.put("_id", getIdVilleByName(((CarteVilleJoueur) carte).getLaVille().getNom()));
                    cartesVille.add(carteDoc);
                } else if (carte instanceof CarteEvenement) {
                    cartesEvenement.add(((CarteEvenement) carte).getTypeEvenement().name());
                }
            }
            joueurDoc.put("cartesVille", cartesVille);
            joueurDoc.put("cartesEvenement", cartesEvenement);
            joueurs.add(joueurDoc);
        }
        doc.put("joueurs", joueurs);

        // Création de la liste de maladies
        List<Document> maladies = new ArrayList<>();
        for (Maladie maladie : partie.getPlateau().getMaladies()) {
            // Création d'un document pour chaque maladie
            Document maladieDoc = new Document();
            maladieDoc.put("_id", getIdMaladieByName(maladie.getCouleur()));
            maladieDoc.put("cubesRestants", maladie.getCubesRestants());
            maladieDoc.put("remede", maladie.remedeEtabli());
            maladieDoc.put("eradique", maladie.isEradique());
            maladies.add(maladieDoc);
        }
        docPlateau.put("maladies", maladies);

        // Création de la liste de stations
        List<Document> stations = new ArrayList<>();
        for (Ville ville : partie.getPlateau().getLesVilles()) {
            if (ville.hasStationDeRecherche()) {
                Document stationDoc = new Document();
                stationDoc.put("_id", getIdVilleByName(ville.getNom()));
                stations.add(stationDoc);
            }
        }
        docPlateau.put("stations", stations);

        docPlateau.put("compteurEclosion", partie.getPlateau().getCompteurEclosion());
        docPlateau.put("indicePropagation", partie.getPlateau().getIndicePropagation());

        // Création de la liste de defausseJoueur
        List<Document> defausseJoueurVille = new ArrayList<>();
        List<String> defausseJoueurEvenement = new ArrayList<>();
        int defausseJoueurEpidemie = 0;
        for (ICarteJoueur carte : partie.getPlateau().getDefausseJoueur()) {
            if (carte instanceof CarteVilleJoueur) {
                Document carteDoc = new Document();
                String villeCarte = ((CarteVilleJoueur) carte).getLaVille().getNom();
                carteDoc.put("_id", getIdVilleByName(villeCarte));
                defausseJoueurVille.add(carteDoc);
            } else if (carte instanceof CarteEpidemie) {
                defausseJoueurEpidemie++;
            } else if (carte instanceof CarteEvenement) {
                defausseJoueurEvenement.add(((CarteEvenement) carte).getTypeEvenement().name());
            }
        }
        docPlateau.put("defausseJoueurVille", defausseJoueurVille);
        docPlateau.put("defausseJoueurEvenement", defausseJoueurEvenement);
        docPlateau.put("defausseJoueurEpidemie", defausseJoueurEpidemie);

        // Création de la liste de piocheJoueur
        List<Document> piocheJoueurVille = new ArrayList<>();
        List<String> piocheJoueurEvenement = new ArrayList<>();
        int piocheJoueurEpidemie = 0;
        for (ICarteJoueur carte : partie.getPlateau().getPiocheJoueur()) {
            if (carte instanceof CarteVilleJoueur) {
                Document carteDoc = new Document();
                String villeCarte = ((CarteVilleJoueur) carte).getLaVille().getNom();
                carteDoc.put("_id", getIdVilleByName(villeCarte));
                piocheJoueurVille.add(carteDoc);
            } else if (carte instanceof CarteEpidemie) {
                piocheJoueurEpidemie++;
            } else if (carte instanceof CarteEvenement) {
                piocheJoueurEvenement.add(((CarteEvenement) carte).getTypeEvenement().name());
            }
        }
        docPlateau.put("piocheJoueurVille", piocheJoueurVille);
        docPlateau.put("piocheJoueurEvenement", piocheJoueurEvenement);
        docPlateau.put("piocheJoueurEpidemie", piocheJoueurEpidemie);

        // Création de la liste de defaussePropagation
        List<Document> defaussePropagation = new ArrayList<>();
        for (CartePropagation carte : partie.getPlateau().getDefaussePropagation()) {
            Document carteDoc = new Document();
            String villeCarte = carte.getLaVille().getNom();
            carteDoc.put("_id", getIdVilleByName(villeCarte));
            defaussePropagation.add(carteDoc);
        }
        docPlateau.put("defaussePropagation", defaussePropagation);

        // Création de la liste de piochePropagation
        List<Document> piochePropagation = new ArrayList<>();
        for (CartePropagation carte : partie.getPlateau().getPiochePropagation()) {
            Document carteDoc = new Document();
            String villeCarte = carte.getLaVille().getNom();
            carteDoc.put("_id", getIdVilleByName(villeCarte));
            piochePropagation.add(carteDoc);
        }
        docPlateau.put("piochePropagation", piochePropagation);
        doc.put("plateau", docPlateau);
        doc.put("partieTerminee", partie.partieTerminee());

        Bson filters = Filters.eq("_id", "partie:" + partie.getIdPartie());
        partiesMongoCollection.deleteOne(filters);
        partiesMongoCollection.insertOne(doc);

    }

    public List<CartePropagation> getCartesPropagation() {

        List<CartePropagation> cartes = new ArrayList<>();
        for (Ville v : this.mapVilles.values()) {
            cartes.add(new CartePropagation(v));
        }
        return cartes;
    }

    public List<ICarteJoueur> getCartesEvenementsJoueur() {
        return Arrays.asList(
                new CarteEvenement(TypeEvenement.PAR_UNE_NUIT_TRANQUILLE),
                new CarteEvenement(TypeEvenement.PREVISION),
                new CarteEvenement(TypeEvenement.PONT_AERIEN),
                new CarteEvenement(TypeEvenement.POPULATION_RESILIENTE),
                new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE)
        );
    }

    public List<ICarteJoueur> getCartesVilleJoueur() {
        List<ICarteJoueur> cartes = new ArrayList<>();
        for (Ville v : this.mapVilles.values()) {
            cartes.add(new CarteVilleJoueur(v));
        }
        return cartes;
    }

    public List<ICarteJoueur> getCartesEpidemieJoueur() {
        List<ICarteJoueur> cartes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cartes.add(new CarteEpidemie());
        }
        return cartes;
    }

    public Ville getVille(String name) {
        return this.mapVilles.get(name);
    }

    private void ajouterVillesAlentours() throws VilleVoisineAElleMemeException,
                                                         VilleDoublonVoisinException {
        // Relier Villes
        // - Bleues -
        getVille("Atlanta").ajouterVillesAlentours(
                getVille("Chicago"), getVille("Washington"), getVille("Miami")
        );

        getVille("Chicago").ajouterVillesAlentours(
                getVille("San Francisco"), getVille("Atlanta"),
                getVille("Montreal"),
                getVille("Los Angeles"),
                getVille("Mexico")
        );

        getVille("Essen").ajouterVillesAlentours(
                getVille("Londres"),
                getVille("Paris"),
                getVille("Milan"),
                getVille("Saint-Pétersbourg"));

        getVille("Londres").ajouterVillesAlentours(
                getVille("New-York"), getVille("Paris"),
                getVille("Madrid"), getVille("Essen"));

        getVille("Madrid").ajouterVillesAlentours(
                getVille("Londres"),
                getVille("Paris"),
                getVille("New-York"),
                getVille("Alger"),
                getVille("São Paulo"));

        getVille("Milan").ajouterVillesAlentours(
                getVille("Essen"),
                getVille("Paris"),
                getVille("Istanbul"));
        getVille("Montreal").ajouterVillesAlentours(
                getVille("New-York"),
                getVille("Washington"),
                getVille("Chicago"));

        getVille("Paris").ajouterVillesAlentours(
                getVille("Madrid"),
                getVille("Alger"),
                getVille("Milan"),
                getVille("Essen"),
                getVille("Londres"));

        getVille("Saint-Pétersbourg").ajouterVillesAlentours(
                getVille("Moscou"), getVille("Istanbul"),
                getVille("Essen"));

        getVille("San Francisco").ajouterVillesAlentours(
                getVille("Chicago"),
                getVille("Los Angeles"),
                getVille("Tokyo"),
                getVille("Manille"));

        getVille("New-York").ajouterVillesAlentours(
                getVille("Londres"),
                getVille("Madrid"),
                getVille("Washington"),
                getVille("Montreal"));

        getVille("Washington").ajouterVillesAlentours(
                getVille("New-York"),
                getVille("Montreal"),
                getVille("Atlanta"),
                getVille("Miami"));
        // - Jaunes -
        getVille("Bogotá").ajouterVillesAlentours(
                getVille("Miami"),
                getVille("São Paulo"),
                getVille("Buenos Aires"),
                getVille("Lima"),
                getVille("Mexico"));

        getVille("Buenos Aires").ajouterVillesAlentours(
                getVille("São Paulo"),
                getVille("Bogotá"));
        getVille("Johannesburg").ajouterVillesAlentours(
                getVille("Kinshasa"),
                getVille("Khartoum"));

        getVille("Khartoum").ajouterVillesAlentours(
                getVille("Johannesburg"),
                getVille("Kinshasa"),
                getVille("Lagos"),
                getVille("Le Caire"));
        getVille("Kinshasa").ajouterVillesAlentours(
                getVille("Lagos"),
                getVille("Johannesburg"),
                getVille("Khartoum"));

        getVille("Lagos").ajouterVillesAlentours(
                getVille("São Paulo"),
                getVille("Khartoum"),
                getVille("Kinshasa"));
        getVille("Lima").ajouterVillesAlentours(
                getVille("Santiago"),
                getVille("Bogotá"),
                getVille("Mexico"));
        getVille("Los Angeles").ajouterVillesAlentours(
                getVille("San Francisco"),
                getVille("Mexico"),
                getVille("Chicago"),
                getVille("Sydney"));
        getVille("Mexico").ajouterVillesAlentours(
                getVille("Los Angeles"),
                getVille("Chicago"),
                getVille("Miami"),
                getVille("Bogotá"),
                getVille("Lima"));
        getVille("Miami").ajouterVillesAlentours(
                getVille("Washington"),
                getVille("Atlanta"),
                getVille("Mexico"),
                getVille("Bogotá"));
        getVille("Santiago").ajouterVillesAlentours(getVille("Lima"));
        getVille("São Paulo").ajouterVillesAlentours(
                getVille("Buenos Aires"),
                getVille("Bogotá"),
                getVille("Madrid"),
                getVille("Lagos"));
        //Noirs
        this.getVille("Alger").ajouterVillesAlentours(
                this.getVille("Madrid"),
                this.getVille("Paris"),
                this.getVille("Istanbul"),
                this.getVille("Le Caire")
        );
        this.getVille("Bagdad").ajouterVillesAlentours(
                this.getVille("Istanbul"),
                this.getVille("Le Caire"),
                this.getVille("Téhéran"),
                this.getVille("Karachi"),
                this.getVille("Riyad")
        );
        this.getVille("Calcutta").ajouterVillesAlentours(
                this.getVille("Delhi"),
                this.getVille("Chennai"),
                this.getVille("Bangkok"),
                this.getVille("Hong-Kong")
        );
        this.getVille("Chennai").ajouterVillesAlentours(
                this.getVille("Calcutta"),
                this.getVille("Bangkok"),
                this.getVille("Jakarta"),
                this.getVille("Mumbai"),
                this.getVille("Delhi")
        );
        this.getVille("Delhi").ajouterVillesAlentours(
                this.getVille("Téhéran"),
                this.getVille("Karachi"),
                this.getVille("Mumbai"),
                this.getVille("Chennai"),
                this.getVille("Calcutta")
        );
        this.getVille("Istanbul").ajouterVillesAlentours(
                this.getVille("Milan"),
                this.getVille("Saint-Pétersbourg"),
                this.getVille("Alger"),
                this.getVille("Moscou"),
                this.getVille("Bagdad"),
                this.getVille("Le Caire")
        );
        this.getVille("Karachi").ajouterVillesAlentours(
                this.getVille("Bagdad"),
                this.getVille("Téhéran"),
                this.getVille("Riyad"),
                this.getVille("Delhi"),
                this.getVille("Mumbai")
        );
        this.getVille("Le Caire").ajouterVillesAlentours(
                this.getVille("Khartoum"),
                this.getVille("Alger"),
                this.getVille("Istanbul"),
                this.getVille("Bagdad"),
                this.getVille("Riyad")
        );
        this.getVille("Moscou").ajouterVillesAlentours(
                this.getVille("Saint-Pétersbourg"),
                this.getVille("Téhéran"),
                this.getVille("Istanbul")
        );
        this.getVille("Mumbai").ajouterVillesAlentours(
                this.getVille("Karachi"),
                this.getVille("Delhi"),
                this.getVille("Chennai")
        );
        this.getVille("Riyad").ajouterVillesAlentours(
                this.getVille("Le Caire"),
                this.getVille("Bagdad"),
                this.getVille("Karachi")
        );
        this.getVille("Téhéran").ajouterVillesAlentours(
                this.getVille("Moscou"),
                this.getVille("Bagdad"),
                this.getVille("Karachi"),
                this.getVille("Delhi")
        );
//Rouges
        this.getVille("Bangkok").ajouterVillesAlentours(
                this.getVille("Calcutta"),
                this.getVille("Chennai"),
                this.getVille("Jakarta"),
                this.getVille("Hong-Kong"),
                this.getVille("Hô-Chi-Minh-Ville")
        );
        this.getVille("Hô-Chi-Minh-Ville").ajouterVillesAlentours(
                this.getVille("Jakarta"),
                this.getVille("Bangkok"),
                this.getVille("Hong-Kong"),
                this.getVille("Manille")
        );
        this.getVille("Hong-Kong").ajouterVillesAlentours(
                this.getVille("Shanghai"),
                this.getVille("Taipei"),
                this.getVille("Hô-Chi-Minh-Ville"),
                this.getVille("Bangkok"),
                this.getVille("Calcutta"),
                this.getVille("Manille")
        );
        this.getVille("Jakarta").ajouterVillesAlentours(
                this.getVille("Chennai"),
                this.getVille("Sydney"),
                this.getVille("Hô-Chi-Minh-Ville"),
                this.getVille("Bangkok")
        );
        this.getVille("Manille").ajouterVillesAlentours(
                this.getVille("Sydney"),
                this.getVille("Hô-Chi-Minh-Ville"),
                this.getVille("Hong-Kong"),
                this.getVille("Taipei"),
                this.getVille("San Francisco")
        );
        this.getVille("Osaka").ajouterVillesAlentours(
                this.getVille("Tokyo"),
                this.getVille("Taipei")
        );
        this.getVille("Pékin").ajouterVillesAlentours(
                this.getVille("Séoul"),
                this.getVille("Shanghai")
        );
        this.getVille("Séoul").ajouterVillesAlentours(
                this.getVille("Tokyo"),
                this.getVille("Pékin"),
                this.getVille("Shanghai")
        );
        this.getVille("Shanghai").ajouterVillesAlentours(
                this.getVille("Pékin"),
                this.getVille("Séoul"),
                this.getVille("Tokyo"),
                this.getVille("Taipei"),
                this.getVille("Hong-Kong")
        );
        this.getVille("Sydney").ajouterVillesAlentours(
                this.getVille("Manille"),
                this.getVille("Jakarta"),
                this.getVille("Los Angeles")
        );
        this.getVille("Taipei").ajouterVillesAlentours(
                this.getVille("Manille"),
                this.getVille("Hong-Kong"),
                this.getVille("Shanghai"),
                this.getVille("Osaka")
        );
        this.getVille("Tokyo").ajouterVillesAlentours(
                this.getVille("Séoul"),
                this.getVille("Shanghai"),
                this.getVille("Osaka"),
                this.getVille("San Francisco")
        );
    }

    public List<Maladie> getMaladies() {
        initMaladies();
        return new ArrayList<>(mapMaladie.values());
    }
    public List<Ville> getVilles() throws VilleVoisineAElleMemeException, VilleDoublonVoisinException {
        initVilles();
        return new ArrayList<>(mapVilles.values());
    }

    public Maladie getMaladieByname(String name){
        initMaladies();
        return mapMaladie.get(name);
    }

}

