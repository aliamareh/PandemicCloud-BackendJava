package donnees;

import modele.Maladie;
import modele.Ville;
import modele.cartes.*;
import modele.exceptions.VilleDoublonVoisinException;
import modele.exceptions.VilleMaladiesDejaInitialiseesException;
import modele.exceptions.VilleVoisineAElleMemeException;

import java.util.*;

public class BibDonneesBrut {

    private static boolean villeAlentour = false;
    private static boolean maladieInit = false;

    private static final Maladie bleue = new Maladie("Bleue");
    private static final Maladie jaune = new Maladie("Jaune");
    private static final Maladie rouge = new Maladie("Rouge");
    private static final Maladie noire = new Maladie("Noire");
    private static final List<Maladie> lesMaladies = new ArrayList<>();

    //Créer Villes
    // - Bleues -
   private static final Ville atlanta = new Ville("Atlanta", 475000, bleue);
    private static final Ville chicago = new Ville("Chicago", 9121000, bleue);
   private static final Ville essen = new Ville("Essen", 575000, bleue);
    private static final Ville londres = new Ville("Londres", 8586000, bleue);
    private static final Ville madrid = new Ville("Madrid",5427000, bleue);
    private static final Ville milan = new Ville("Milan", 5232000, bleue);
   private static final Ville montreal = new Ville("Montreal", 3429000, bleue);
    private static final Ville paris = new Ville("Paris", 10755000, bleue);
    private static final Ville stpetersbourg = new Ville("Saint-Pétersbourg", 4879000, bleue);
    private static final Ville sanfrancisco = new Ville("San Francisco", 5864000, bleue);
   private static final Ville newyork = new Ville("New-York", 20464000, bleue);
    private static final Ville washington = new Ville("Washington", 4679000, bleue);
    // - Jaunes -
    private static final Ville bogota = new Ville("Bogotá",8702000, jaune);
    private static final Ville buenosaires = new Ville("Buenos Aires", 13639000, jaune);
    private static final Ville johannesburg = new Ville("Johannesburg", 3888000, jaune);
    private static final Ville khartoum = new Ville("Khartoum", 4887000, jaune);
    private static final Ville kinshasa = new Ville("Kinshasa", 9046000, jaune);
    private static final Ville lagos = new Ville("Lagos",11547000, jaune);
    private static final Ville lima = new Ville("Lima", 9121000, jaune);
    private static final Ville losangeles = new Ville("Los Angeles",14900000, jaune);
   private static final Ville mexico = new Ville("Mexico", 19463000, jaune);
    private static final Ville miami = new Ville("Miami", 5582000, jaune);
    private static final Ville santiago = new Ville("Santiago",6015000, jaune);
    private static final Ville saopaulo = new Ville("São Paulo", 20186000, jaune);
    // - Noires -
    private static final Ville alger = new Ville("Alger",2946000, noire);
    private static final Ville bagdad = new Ville("Bagdad", 6204000, noire);
    private static final Ville calcutta = new Ville("Calcutta", 14374000, noire);
    private static final Ville chennai = new Ville("Chennai", 8865000, noire);
    private static final Ville delhi = new Ville("Delhi", 22242000, noire);
    private static final Ville istanbul = new Ville("Istanbul",13576000, noire);
    private static final Ville karachi = new Ville("Karachi", 20711000, noire);
    private static final Ville lecaire = new Ville("Le Caire",14718000, noire);
    private static final Ville moscou = new Ville("Moscou", 15512000, noire);
    private static final Ville mumbai = new Ville("Mumbai", 16910000, noire);
    private static final Ville riyad = new Ville("Riyad",5037000, noire);
    private static final Ville teheran = new Ville("Téhéran", 7419000, noire);
    // - Rouges -
    public static final Ville bangkok = new Ville("Bangkok",7151000, rouge);
    public static final Ville hochiminhville = new Ville("Hô-Chi-Minh-Ville", 8314000, rouge);
    public static final Ville hongkong = new Ville("Hong-Kong", 7106000, rouge);
    public static final Ville jakarta = new Ville("Jakarta", 26063000, rouge);
    public static final Ville manille = new Ville("Manille", 20767000, rouge);
    public static final Ville osaka = new Ville("Osaka",2871000, rouge);
    public static final Ville pekin = new Ville("Pékin", 17311000, rouge);
    public static final Ville seoul = new Ville("Séoul",22547000, rouge);
    public static final Ville shanghai = new Ville("Shanghai", 13482000, rouge);
    public static final Ville sydney = new Ville("Sydney", 3785000, rouge);
    public static final Ville taipei = new Ville("Taipei",8338000, rouge);
    public static final Ville tokyo = new Ville("Tokyo", 13139000, rouge);
    public static void initMaladies(){
            lesMaladies.clear();
            lesMaladies.addAll(
                    Arrays.asList(
                            new Maladie(bleue.getCouleur()),
                            new Maladie(jaune.getCouleur()),
                            new Maladie(rouge.getCouleur()),
                            new Maladie(noire.getCouleur()))
            );
            maladieInit = true;
    }
    public static List<Maladie> getMaladies() {
        if(! maladieInit) {
            initMaladies();
        }
        return lesMaladies;
    }

    public static Maladie getMaladieByname(String name){
        getMaladies();
        return lesMaladies.stream().filter( m -> m.getCouleur().equals(name)).findFirst().get();
    }



    public static List<Ville> getVilles() throws VilleMaladiesDejaInitialiseesException,
            VilleVoisineAElleMemeException, VilleDoublonVoisinException
    {
        if(! villeAlentour){
            ajouterVillesAlentours();
            villeAlentour = true;
        }
        List<Ville> listeVilles = Arrays.asList(
                atlanta,
                chicago,
                essen,
                londres,
                madrid,
                milan,
                montreal,
                paris,
                stpetersbourg,
                sanfrancisco,
                newyork,
                washington,
                bogota,
                buenosaires,
                johannesburg,
                khartoum,
                kinshasa,
                lagos,
                lima,
                losangeles,
                mexico,
                miami,
                santiago,
                saopaulo,
                alger,
                bagdad,
                calcutta,
                chennai,
                delhi,
                istanbul,
                karachi,
                lecaire,
                moscou,
                mumbai,
                riyad,
                teheran,
                bangkok,
                hochiminhville,
                hongkong,
                jakarta,
                manille,
                osaka,
                pekin,
                seoul,
                shanghai,
                sydney,
                taipei,
                tokyo
        );
        List<Ville> res = new ArrayList<>();
        Set<Ville> villesDejaInit = new HashSet<>();
        for (Ville v : listeVilles){

            boolean isVilleEstPresente = false;
            Ville villeTemp = null;
            for (Ville ville1 : villesDejaInit ){
                if(v.getNom().equals(ville1.getNom())){
                    villeTemp = ville1;
                    isVilleEstPresente = true;
                }
            }
            if(! isVilleEstPresente){
                villeTemp = new Ville(v.getNom(),v.getPopulation(),getMaladieByname(v.getMaladieParDefaut().getCouleur()));
                villesDejaInit.add(villeTemp);
            }

            Ville finalVilleTemp = villeTemp;
            v.getVillesAlentours().forEach(ville -> {
                try {
                    boolean isVilleDejaInit = false;
                    for (Ville ville1 : villesDejaInit ){
                        if(ville1.getNom().equals(ville.getNom())){
                            finalVilleTemp.ajouterVillesAlentours(ville1);
                            isVilleDejaInit = true;
                        }
                    }
                    if(! isVilleDejaInit){
                        Ville newInitVille = new Ville(ville.getNom(),ville.getPopulation(),getMaladieByname(ville.getMaladieParDefaut().getCouleur()));
                        finalVilleTemp.ajouterVillesAlentours(newInitVille);
                        villesDejaInit.add(newInitVille);
                    }
                }
                catch (VilleVoisineAElleMemeException | VilleDoublonVoisinException e) {
                    throw new RuntimeException(e);
                }
            });
            res.add(villeTemp);
        }
        maladieInit = false;
        return res;
    }



    private  static void  ajouterVillesAlentours() throws VilleVoisineAElleMemeException,
            VilleDoublonVoisinException {
        // Relier Villes
        // - Bleues -
        atlanta.ajouterVillesAlentours(chicago, washington, miami);
        chicago.ajouterVillesAlentours(sanfrancisco, atlanta, montreal, losangeles, mexico);
        essen.ajouterVillesAlentours(londres, paris, milan, stpetersbourg);
        londres.ajouterVillesAlentours(newyork, paris, madrid, essen);
        madrid.ajouterVillesAlentours(londres, paris, newyork, alger, saopaulo);
        milan.ajouterVillesAlentours(essen,paris,istanbul);
        montreal.ajouterVillesAlentours(newyork, washington, chicago);
        paris.ajouterVillesAlentours(madrid, alger, milan, essen, londres);
        stpetersbourg.ajouterVillesAlentours(moscou,istanbul,essen);
        sanfrancisco.ajouterVillesAlentours(chicago,losangeles,tokyo, manille);
        newyork.ajouterVillesAlentours(londres, madrid, washington, montreal);
        washington.ajouterVillesAlentours(newyork, montreal, atlanta, miami);
        // - Jaunes -
        bogota.ajouterVillesAlentours(miami, saopaulo, buenosaires, lima, mexico);
        buenosaires.ajouterVillesAlentours(saopaulo, bogota);
        johannesburg.ajouterVillesAlentours(kinshasa, khartoum);
        khartoum.ajouterVillesAlentours(johannesburg, kinshasa, lagos,lecaire);
        kinshasa.ajouterVillesAlentours(lagos, johannesburg, khartoum);
        lagos.ajouterVillesAlentours(saopaulo,khartoum, kinshasa);
        lima.ajouterVillesAlentours(santiago, bogota, mexico);
        losangeles.ajouterVillesAlentours(sanfrancisco, mexico, chicago, sydney);
        mexico.ajouterVillesAlentours(losangeles, chicago, miami, bogota, lima);
        miami.ajouterVillesAlentours(washington, atlanta, mexico, bogota);
        santiago.ajouterVillesAlentours(lima);
        saopaulo.ajouterVillesAlentours(buenosaires, bogota, madrid, lagos);
        // - Noires -
        alger.ajouterVillesAlentours(madrid, paris, istanbul, lecaire);
        bagdad.ajouterVillesAlentours(istanbul, lecaire, teheran, karachi, riyad);
        calcutta.ajouterVillesAlentours(delhi, chennai, bangkok, hongkong);
        chennai.ajouterVillesAlentours(calcutta, bangkok, jakarta, mumbai, delhi);
        delhi.ajouterVillesAlentours(teheran, karachi, mumbai, chennai, calcutta);
        istanbul.ajouterVillesAlentours(milan, stpetersbourg, alger, moscou, bagdad, lecaire);
        karachi.ajouterVillesAlentours(bagdad, teheran, riyad, delhi, mumbai);
        lecaire.ajouterVillesAlentours(khartoum, alger, istanbul, bagdad, riyad);
        moscou.ajouterVillesAlentours(stpetersbourg, teheran, istanbul);
        mumbai.ajouterVillesAlentours(karachi, delhi, chennai);
        riyad.ajouterVillesAlentours(lecaire, bagdad, karachi);
        teheran.ajouterVillesAlentours(moscou, bagdad, karachi, delhi);
        // - Rouges -
        bangkok.ajouterVillesAlentours(calcutta,chennai,jakarta,hongkong,hochiminhville);
        hochiminhville.ajouterVillesAlentours(jakarta,bangkok,hongkong,manille);
        hongkong.ajouterVillesAlentours(shanghai,taipei, hochiminhville, bangkok, calcutta, manille);
        jakarta.ajouterVillesAlentours(chennai, sydney, hochiminhville, bangkok);
        manille.ajouterVillesAlentours(sydney, hochiminhville, hongkong, taipei, sanfrancisco);
        osaka.ajouterVillesAlentours(tokyo, taipei);
        pekin.ajouterVillesAlentours(seoul, shanghai);
        seoul.ajouterVillesAlentours(tokyo, pekin, shanghai);
        shanghai.ajouterVillesAlentours(pekin, seoul, tokyo, taipei, hongkong);
        sydney.ajouterVillesAlentours(manille, jakarta, losangeles);
        taipei.ajouterVillesAlentours(manille, hongkong, shanghai, osaka);
        tokyo.ajouterVillesAlentours(seoul, shanghai, osaka, sanfrancisco);
    }


    public static List<CartePropagation> getCartesPropagation(List<Ville> villes) throws VilleMaladiesDejaInitialiseesException, VilleVoisineAElleMemeException, VilleDoublonVoisinException {

        List<CartePropagation> cartes = new ArrayList<>();
        for (Ville v : villes){
            cartes.add(new CartePropagation(v));
        }
        return cartes;
    }

    public static List<ICarteJoueur> getCartesEvenementsJoueur() {
        return Arrays.asList(
                new CarteEvenement(TypeEvenement.PAR_UNE_NUIT_TRANQUILLE),
                new CarteEvenement(TypeEvenement.PREVISION),
                new CarteEvenement(TypeEvenement.PONT_AERIEN),
                new CarteEvenement(TypeEvenement.POPULATION_RESILIENTE),
                new CarteEvenement(TypeEvenement.SUBVENTION_PUBLIQUE)
        );
    }

    public static List<ICarteJoueur> getCartesVilleJoueur(List<Ville> villes) throws VilleMaladiesDejaInitialiseesException, VilleVoisineAElleMemeException, VilleDoublonVoisinException {
        List<ICarteJoueur> cartes = new ArrayList<>();
        for (Ville v : villes){
            cartes.add(new CarteVilleJoueur(v));
        }
        return cartes;
    }

    public static List<ICarteJoueur> getCartesEpidemieJoueur(int nbCartesEpidemie) {
        List<ICarteJoueur> cartes = new ArrayList<>();
        for (int i= 0;i<nbCartesEpidemie;i++){
            cartes.add(new CarteEpidemie());
        }
        return cartes;
    }

}
