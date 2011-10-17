import java.awt.Point;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TestiRobotti extends Robotti {
	
    private Map<Point,int[]> labynTiedot;
    private Point nykyinenSijainti;
    private int[] etenemisVaihtoehdot;
    private boolean tullaanUmpikujasta;
    private Deque<Point> historia;
    
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
    
    public TestiRobotti() {
        super();
        this.nykyinenSijainti = new Point();
        this.labynTiedot = new HashMap<Point, int[]>();
        this.etenemisVaihtoehdot = new int[5];
        this.tullaanUmpikujasta = false;
        this.historia = new ArrayDeque<Point>();
        //this.historia = new LinkedList<Point>();
    }
    
    public void tallennaTiedot() {
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

	private int paataSuunta() {
		/*
		 * ideana siis: jos joka suunnassa on vain ruutuja joissa ollaan käyty -> liiku takaisin historiassa kunnes löytyy uusi vaihtoehto
		 */
		int parasSuunta = Arrays.asList(this.etenemisVaihtoehdot).indexOf(1); //eka vaan joku jonne voi mennä
		int parasSuuntaKertaa = Integer.MAX_VALUE;
		
    	if(jokaSuuntaanOllaanMenty(this.nykyinenSijainti) && historia.peekLast() != null) {
    		this.optiomoiHistoriaa(); //XXX Tää tehostaisi jonkin verran jos toimii!
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

	private void optiomoiHistoriaa() {
		LinkedList<Point> histamiini = new LinkedList<Point>(historia); //kopioidaan historia LinkedList:iksi
		Iterator<Point> iteraattori = histamiini.descendingIterator(); //iteraattori joka menee lopusta alkuun
		Point seuraava = iteraattori.next();
		while(jokaSuuntaanOllaanMenty( seuraava ) && iteraattori.hasNext()) {
			for(Point mahdNaapuri : histamiini) {
				if(naapuritPisteessa(seuraava).contains(mahdNaapuri) && 
					histamiini.lastIndexOf(mahdNaapuri) + 1 < histamiini.lastIndexOf(seuraava)  ) {
					//poista
					//iteraattori.remove();
					for(int i = histamiini.lastIndexOf(mahdNaapuri) + 1; i < histamiini.lastIndexOf(seuraava); i++){
						histamiini.remove(i);
					}
					//List<Point> loppu = histamiini.subList(histamiini.lastIndexOf(seuraava), histamiini.lastIndexOf(histamiini.peekLast()));
					//histamiini = new LinkedList<Point>( histamiini.subList(0, histamiini.lastIndexOf(mahdNaapuri)) );
					//histamiini.addAll(loppu); //kai näinpäin?
				}
			}
			seuraava = iteraattori.next();
		}
		this.historia = histamiini;
	}
	
	List<Point> naapuritPisteessa(Point piste) {
		List<Point> pisteet = new ArrayList<Point>();
		for(int suunta = 0; suunta < 4; suunta++) {
			if(this.labynTiedot.get(piste)[suunta] == 1) {
				pisteet.add( sijaintiSuunnassa(piste, suunta) );
			}
		}
		return pisteet;
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
    
    public boolean kaynytSuunnassa(Point sijainti, int suunta) {
        Point suuntaSijainti = sijaintiSuunnassa(sijainti, suunta);
        	//System.out.println(suuntaSijainti);//FIXME

        if (this.labynTiedot.containsKey(suuntaSijainti)) {
            return true;
        }
        else {
            return false;
        }
    }
    
    public Point viimeVapaaRisteys() {
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
    
    /*void liikuTakaisinUuteenRisteykseen() {
    	if(jokaSuuntaanOllaanMenty(this.nykyinenSijainti)) {
    		suunta = historia.pollLast();
    	}
    }*/
    
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
    
    /*
  private void optiomoiHistoriaa() {
		LinkedList<Point> histamiini = new LinkedList<Point>(historia); //kopioidaan historia LinkedList:iksi
		Iterator<Point> itr = histamiini.descendingIterator();
		while (itr.hasNext()) {
			Point arvo = itr.next();
			for (Point mahdNaapuri : histamiini) {
				if (arvo.equals(mahdNaapuri)) {
					break;
				}
			}
			itr.remove();
	}
		if (histamiini.size() != 0) {
			this.historia = new ArrayDeque<Point>(histamiini);
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
    
    /*private int paataSuunta()_version3 {		 
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
	}*/
    
}
