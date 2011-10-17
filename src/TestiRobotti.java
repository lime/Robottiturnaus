import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class TestiRobotti extends Robotti {
	
    private Random RAND;
    private Map<Point,int[]> labynTiedot;
    private Point nykyinenSijainti;
    private int[] etenemisVaihtoehdot = new int[5];
    
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
        this.labynTiedot = new HashMap<Point, int[]>();
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
        	// 1 tai 0, ei boolean enää
        	this.etenemisVaihtoehdot[this.annaSuunta()] = this.voiEdeta() ? 1 : 0;
        	//lyhyt versio ;)
            this.kaannyVasemmalle();
        }
        if (this.labynTiedot.containsKey(this.nykyinenSijainti)) {
        	//tosi sekavaa, mutta tallentaa montako kertaa ollaan käyty tässä ruudussa
        	this.etenemisVaihtoehdot[4] = this.labynTiedot.get(this.nykyinenSijainti)[4] + 1;
        } else {
        	this.etenemisVaihtoehdot[4] = 1;
        }
        /* .clone(), muuten koko labynTiedot sisältää kaikissa pisteissä saman Pointin */
        this.labynTiedot.put((Point) this.nykyinenSijainti.clone(),this.etenemisVaihtoehdot.clone());
    }
    
    private int paataSuunta() {
    	//FIXME liian sekavaa :(
    	int parasSuunta, parasSuuntaKertaa;
        for(int suunta = 0; suunta < 4; suunta++) {
            if (this.etenemisVaihtoehdot[suunta] == 1) {
            	//ei ole seinää
            	if( this.labynTiedot.get( sijaintiSuunnassa(this.nykyinenSijainti, suunta) )[4] < parasSuuntaKertaa ){
            		parasSuunta = sijaintiSuunnassa(this.nykyinenSijainti, suunta);
            		parasSuuntaKertaa = this.labyTiedot.get( parasSuunta[0] )[4];
            	}
            	//FIXME System.out.println("Suunta: "+suunta+". Voi edetä: "+this.voiEdeta()+". on käynyt suunnassa: "+this.kaynytSuunnassa(suunta));
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
    
    //TODO Henrin umpikuja-sääntö eli jos läydetään umpikuja poistetaan se suunta vaihtoehtoista!
    
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
    
    /* Liikkuu ihan ok, mutta jumittuu, pitäisi lisätä että 
     * seuraa seinää tai valitsee sen suunnan jossa on käynyt vähemmän.
     */
    /*private int paataSuunta_version2() {
        for(int suunta = 0; suunta < 4; suunta++) {
            this.kaannySuuntaan(suunta); //FIXME vai pitääkö aloittaa nykyisestä
            if (this.voiEdeta() && !this.kaynytSuunnassa(suunta)) {
                //ei ole seinää eikä olla käyty siellä
                return suunta;
            }
        }
        return Arrays.asList(this.etenemisVaihtoehdot).indexOf(true);
        //antaa ensimmäisen mahdollisen vaihtoehdon
    }*/
    
}
