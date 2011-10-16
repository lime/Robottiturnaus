import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestiRobotti extends Robotti {
	
    private Random RAND;
    private Map<Point,Boolean[]> labynTiedot;
    private Point nykyinenSijainti;
    private Boolean[] etenemisVaihtoehdot = new Boolean[4];
    
    @Override
    public void teeSiirto() {

    	this.tallennaTiedot();
        
        int uusiSuunta = this.paataSuunta();
        
        this.kaannySuuntaan(uusiSuunta);
        this.uusiSijainti(uusiSuunta);
        
        System.out.println(this.labynTiedot);
        this.etene();
    }
    
    public TestiRobotti() {
        super();
        this.nykyinenSijainti = new Point();
        this.labynTiedot = new HashMap<Point, Boolean[]>();
    }
    
    public void uusiSijainti(int suunta) {
        this.nykyinenSijainti.setLocation(sijaintiSuunnassa(this.nykyinenSijainti, suunta));
    }
    
    static Point sijaintiSuunnassa(Point sijainti, int suunta) {
    	double uusiX = sijainti.getX();
    	double uusiY = sijainti.getY();
    	switch( suunta ) {
		//koordinaatisto alkaa nääköjään vasemmasta alanurkasta meillä, oisko ylänurkka parempi..?
			case Robotti.POHJOINEN: uusiY++; break;
			case Robotti.ETELA: uusiY--; break;
			case Robotti.LANSI: uusiX--; break;
			case Robotti.ITA: uusiX++; break;
    	}
    	Point pal = new Point();
    	pal.setLocation(uusiX, uusiY);
    	return pal;
    }
    
    public void tallennaTiedot() {
        /* For-loopilla käydään kaikki suunnat läpi ja tarkistetaan, voiko kyseessä
         * olevaan suuntaan edetä
        */
        for (int t=0; t < 4; t++) {
        	this.etenemisVaihtoehdot[this.annaSuunta()] = this.voiEdeta();
        	//lyhyt versio ;)
            this.kaannyVasemmalle();
        }
        /* .clone(), muuten koko labynTiedot sisältää kaikissa pisteissä saman Pointin */
        this.labynTiedot.put((Point) this.nykyinenSijainti.clone(),this.etenemisVaihtoehdot.clone());
    }
    
    private int paataSuunta() {
        for(int suunta = 0; suunta < 4; suunta++) {
            this.kaannySuuntaan(suunta); //FIXME vai pitääkö aloittaa nykyisestä
            if (this.voiEdeta() && !this.kaynytSuunnassa(suunta)) {
                //ei ole seinää eikä olla käyty siellä
               //FIXME sysysSystem.out.println("Suunta: "+suunta+". Voi edetä: "+this.voiEdeta()+". on käynyt suunnassa: "+this.kaynytSuunnassa(suunta));
                return suunta;
            }
        }
        System.out.println("Nope, sitten suuntaan: "+Arrays.asList(this.etenemisVaihtoehdot).indexOf(true));
        return Arrays.asList(this.etenemisVaihtoehdot).indexOf(true);
        //antaa ensimmäisen mahdollisen vaihtoehdon
    }
        
    
    public boolean kaynytSuunnassa(int suunta) {
        Point suuntaSijainti = sijaintiSuunnassa(this.nykyinenSijainti, suunta);
        	System.out.println(suuntaSijainti);//FIXME

        if (this.labynTiedot.containsKey(suuntaSijainti)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    private void kaannySuuntaan(int suunta) {
        while(this.annaSuunta() != suunta) {
        // jos ei ole oikeassa suunnassa niin käännetään
            this.kaannyOikealle();
            //TODO aina oikealle, voisi valita lyhyempi (turhaa)
        }
    }
    
    public static void main(String[] args) {
		Turnaus.main(args);
	}
    
    /*boolean seinaOikealla() {
        kaannyOikealle(); 
        boolean palaute = !this.voiEdeta();
        kaannyVasemmalle();
        return palaute;
    }*/
    /* Seuraa seinää.
     * private int paataSuunta_version1() {
        if( seinaOikealla() && voiEdeta() ) {
            //seinä on siellä, eteenpäin vaan
            return this.annaSuunta();
        }else if( !seinaOikealla() ) {
            //minne seinä meni? mennään perään!
            this.kaannyOikealle();
        }
        else if( !voiEdeta() ) {
            //seinä kääntyi eteen, käännytään sen mukana
            this.kaannyVasemmalle();
        }
            return this.annaSuunta();
    }*/
    
}
