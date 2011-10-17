import java.awt.Point;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

public class PeruuttavaRobotti extends Robotti {
	
    private Map<Point,int[]> labynTiedot;
    private Point nykyinenSijainti;
    private int[] etenemisVaihtoehdot;
    private boolean tullaanUmpikujasta;
    private ArrayDeque<Point> historia;
    
    @Override
    public void teeSiirto() {

    	this.tallennaTiedot();
        
        int uusiSuunta = this.paataSuunta();
                
        this.kaannySuuntaan(uusiSuunta);
        this.uusiSijainti(uusiSuunta);
        
        /*Point viimeVapaa = this.viimeVapaaRisteys();
        if ( !this.nykyinenSijainti.equals(viimeVapaa) ) {
        	System.out.println("viimeisin vapaa risteys: "+viimeVapaa);
        }*/
        
        //System.out.println(this.labynTiedot);
        this.etene();
    }
    
    public PeruuttavaRobotti() {
        super();
        this.nykyinenSijainti = new Point();
        this.labynTiedot = new HashMap<Point, int[]>();
        this.etenemisVaihtoehdot = new int[5];
        this.tullaanUmpikujasta = false;
        this.historia = new ArrayDeque<Point>();
    }
    
    void tallennaTiedot() {
	    /* For-loopilla käydään kaikki suunnat läpi ja tarkistetaan, voiko kyseessä
	     * olevaan suuntaan edetä
	    */
		//vain eka kerta
		if(this.labynTiedot.containsKey(this.nykyinenSijainti)) {
			this.etenemisVaihtoehdot = this.labynTiedot.get(this.nykyinenSijainti);
		} else {
			for (int t=0; t < 4; t++) {
	    	// 1 tai 0, ei boolean enää
	    	this.etenemisVaihtoehdot[this.annaSuunta()] = this.voiEdeta() ? 1 : 0;
	    	//lyhyt versio ;)
	        this.kaannyVasemmalle();
			}
		}
		
		
		if(this.tullaanUmpikujasta){
			//vastakkaiseen ei sitten mennä takas >:3
			this.etenemisVaihtoehdot[(this.annaSuunta()+2) % 4] = 0;
		}
		
		int vaihtoehtoja = 0;
		for(int i = 0; i < 4; i++){
			if (this.etenemisVaihtoehdot[i] == 1){
				vaihtoehtoja++;
			}
		}
		this.tullaanUmpikujasta = vaihtoehtoja > 1 ? false : true;

	    
	    if (this.labynTiedot.containsKey(this.nykyinenSijainti)) {
	    	//tosi sekavaa, mutta tallentaa montako kertaa ollaan käyty tässä ruudussa
	    	this.etenemisVaihtoehdot[4] = this.labynTiedot.get(this.nykyinenSijainti)[4] + 1;
	    } else {
	    	this.etenemisVaihtoehdot[4] = 1;
	    }
	    /* .clone(), muuten koko labynTiedot sisältää kaikissa pisteissä saman Pointin */
	    this.labynTiedot.put((Point) this.nykyinenSijainti.clone(),this.etenemisVaihtoehdot.clone());
	    
	    this.historia.addLast((Point) this.nykyinenSijainti.clone()); //historiaan lisätään että täällä käytiin
	}

	int paataSuunta() {
		/*
		 * ideana siis: 
		 */
		//FIXME liian sekavaa :(
		int parasSuunta = Arrays.asList(this.etenemisVaihtoehdot).indexOf(1); //eka vaan joku jonne voi mennä
		int parasSuuntaKertaa = Integer.MAX_VALUE;
		
    	if(jokaSuuntaanOllaanMenty(this.nykyinenSijainti) && historia.peekLast() != null) {
			Point viimeSijainti = historia.pollLast();
    		while(viimeSijainti.equals(this.nykyinenSijainti) && historia.peekLast() != null) {
    			viimeSijainti = historia.pollLast(); //ettei ole nykyinen -hotfix
    		}
    		
    		parasSuunta = suuntaNaapuriin(nykyinenSijainti, viimeSijainti);
    		System.out.println("**** Ollaan menty jo joka suuntaan, paras suunta nyt olisi: " + parasSuunta + ", jotta päästään takaisin ruutuun: " + viimeSijainti);
    		System.out.println("Nyky: " + this.nykyinenSijainti + ", viime: " + viimeSijainti);
    		return parasSuunta;
    	}
    	
	    for(int suunta = 0; suunta < 4; suunta++) {
	        if (this.etenemisVaihtoehdot[suunta] == 1) { //ei ole seinää
	        	Point suuntaSijainti = sijaintiSuunnassa(nykyinenSijainti,suunta);
	        	if( !kaynytSuunnassa(this.nykyinenSijainti,suunta) ){
	        		parasSuunta = suunta;
	        		parasSuuntaKertaa = 0;
	        	} else if ( this.labynTiedot.get( suuntaSijainti )[4] <= parasSuuntaKertaa ){
	        		parasSuunta = suunta;
	        		parasSuuntaKertaa = this.labynTiedot.get( sijaintiSuunnassa(this.nykyinenSijainti, parasSuunta) )[4];
	        	}
	        }
	    }
	    return parasSuunta;
	}

	void uusiSijainti(int suunta) {
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
    
    boolean kaynytSuunnassa(Point sijainti, int suunta) {
        Point suuntaSijainti = sijaintiSuunnassa(sijainti, suunta);
        	//System.out.println(suuntaSijainti);//FIXME

        if (this.labynTiedot.containsKey(suuntaSijainti)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    Point viimeVapaaRisteys() {
    	boolean loytyiVapaaRisteys = false;
    	while(!loytyiVapaaRisteys) {
    		Point sijainti = historia.pollLast();
    		
    		while(sijainti == this.nykyinenSijainti) {
    			sijainti = historia.pollLast(); //ettei ole nykyinen -hotfix
    		}
    		for(int suunta = 0; suunta < 4; suunta++) {
    			if(this.labynTiedot.containsKey(sijainti) ) {
    				//FIXME debuugausta
	    			if(this.labynTiedot.get(sijainti)[suunta] == 1 && !this.kaynytSuunnassa(sijainti, suunta)) {
	    				System.out.println("Nykyinen sijainti: "+this.nykyinenSijainti);
	    				System.out.println("Sijainnista " + sijainti + " ollaan käyty suunnassa " + suunta + ": " + this.kaynytSuunnassa(sijainti, suunta));
	    				loytyiVapaaRisteys = true;
	    				return sijainti;
	    			}
    			}
    		}
    	}
    	return null;
    }
    
    int suuntaNaapuriin(Point nykyinen, Point naapuri) {
    	for(int i = 0; i < 4; i++) {
    		if (sijaintiSuunnassa(nykyinen,i).equals(naapuri) ) {
    			return i;
    		}
    	}
    	System.err.println(nykyinen + " ja " + naapuri + " eivät ole naapureita.");
    	return -1;
    }
    
    boolean jokaSuuntaanOllaanMenty(Point sijainti) {
    	for(int suunta = 0; suunta < 4; suunta++) {
    		if(!kaynytSuunnassa(sijainti, suunta) && this.labynTiedot.get(sijainti)[suunta] == 1) {
    			return false;
    		}
    	}
    	return true;
    }

	//TODO Henrin umpikuja-sääntö eli jos läydetään umpikuja poistetaan se suunta vaihtoehtoista!
	
	void kaannySuuntaan(int suunta) {
	    while(this.annaSuunta() != suunta) {
	    // jos ei ole oikeassa suunnassa niin käännetään
	        this.kaannyOikealle();
	        //TODO aina oikealle, voisi valita lyhyempi (turhaa)
	    }
	}
}
