
import edu.mit.jwi.item.IPointer;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetConfiguration;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.jlt.util.Language;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class Main {

    static HashMap <String, String> lemmaMap;
    
    public static void main(String[] args) {
        Main m = new Main();
        
        lemmaMap = getLemmaMap();
        //Variabile che conterrá le configurazioni base di Babelnet
        BabelNetConfiguration conf = BabelNetConfiguration.getInstance();
        conf.setConfigurationFile(new File("config/babelnet.properties"));

        BabelNet bn = BabelNet.getInstance();
//        Properties props = new Properties();
//        props.put("annotators", "tokenize, ssplit, pos, lemma");
//        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        //array di contesti per la parola pianta e testa
        String[] contestiPianta = {
            "La pianta dell ' alloggio è disponibile in portineria ; dal disegno è possibile cogliere i dettagli dell ' architettura dello stabile , la distribuzione dei vani e la disposizione di porte e finestre .",
            "I platani sono piante ad alto fusto , organismi viventi : non ha senso toglierli per fare posto a un parcheggio ."};
        String[] contestiTesta = {
            "Si tratta di un uomo facilmente riconoscibile : ha una testa piccola , gli occhi sporgenti , naso adunco e piccole orecchie a sventola .",
            "Come per tutte le cose , ci vorrebbe un po ' di testa , un po ' di ragione , cervello , per decidere con il bene dell ' intelletto ."};

        String[] words = {"pianta", "testa"};

        //aggiunta dei contesti pianta nella lista principale
        ArrayList<String> lista_contesti_pianta = new ArrayList<String>();
        lista_contesti_pianta.addAll(Arrays.asList(contestiPianta));
         //aggiunta dei contesti testa nella lista principale
        ArrayList<String> lista_contesti_testa = new ArrayList<String>();
        lista_contesti_testa.addAll(Arrays.asList(contestiTesta));
        
        //rimuoviamo le stopwords a contesti pianta
        ArrayList<ArrayList<String>> contesti_pianta_noStopwords = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < lista_contesti_pianta.size(); i++)
        {
            contesti_pianta_noStopwords.add(removeStopwordsFromContexts(lista_contesti_pianta.get(i)));
        }
        
        ArrayList<ArrayList<String>> lista_lemmi_contesti_pianta = getLemsFromContexts(contesti_pianta_noStopwords);
//        System.out.println(lista_lemmi_contesti_pianta);
        
        //rimuoviamo le stopwords a contesti testa
        ArrayList<ArrayList<String>> contesti_testa_noStopwords = new ArrayList<ArrayList<String>>();
        for(int i = 0; i < lista_contesti_testa.size(); i++)
        {
            contesti_testa_noStopwords.add(removeStopwordsFromContexts(lista_contesti_testa.get(i)));
        }
        
        ArrayList<ArrayList<String>> lista_lemmi_contesti_testa = getLemsFromContexts(contesti_testa_noStopwords);
//        System.out.println(lista_lemmi_contesti_testa);

    }

    public static Set<String> getStopwords() {
        Set<String> stopWords = new LinkedHashSet<String>();
        BufferedReader SW;
        try {
            String line;
            SW = new BufferedReader(new FileReader("src/stopwords.txt"));
            while ((line = SW.readLine()) != null) {
                if (line.indexOf("|") < 0 && !line.equals("")) {
                    stopWords.add(line);
                }
            }
            SW = new BufferedReader(new FileReader("resources/jlt/stopwords/stopwords_it.txt"));
            while ((line = SW.readLine()) != null) {
                stopWords.add(line);
            }
            SW.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return stopWords;
    }

    public static ArrayList<String> removeStopwordsFromContexts(String frase) {
        ArrayList<String> array_tokens = new ArrayList<>();
        Set<String> stopwords = getStopwords();
        String[] words = frase.split("[\\s]+");
        for (String word : words) {
            if (!stopwords.contains(word.toLowerCase())) {
                array_tokens.add(word);
            }
        }

        return array_tokens;
    }
    
    public static HashMap<String,String> getLemmaMap() {
        HashMap<String,String> lemmaMap = new HashMap<String,String>();
        BufferedReader SW;
        try {
            String line;
            SW = new BufferedReader(new FileReader("src/morphit/morph-it_048.txt"));
            while ((line = SW.readLine()) != null) {
                if (line.indexOf("|") < 0 && !line.equals("")) {
                    String[] row = line.split("[\\s]+");
                    lemmaMap.put(row[0], row[1]);
                }
            }
            SW.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return lemmaMap;
    }
    
    public static ArrayList<ArrayList<String>> getLemsFromContexts(ArrayList<ArrayList<String>> lems){
         ArrayList<ArrayList<String>> lista_lemmi_contesti = new ArrayList<>();
        
        for (int i = 0; i < lems.size(); i++) {
            ArrayList<String> lemmi_contesto = new ArrayList<>();
            for(int j = 0; j < lems.get(i).size(); j++){
                 String lemma = lemmaMap.get(lems.get(i).get(j));
                if(lemma != null)
                     lemmi_contesto.add(lemma);
                else
                    lemmi_contesto.add(lems.get(i).get(j));
            }
            lista_lemmi_contesti.add(lemmi_contesto);
        }
        return lista_lemmi_contesti;
    }
//  
//    public ArrayList<ArrayList<String>> getLemsFromContexts(ArrayList<String> contexts, StanfordCoreNLP pipeline) {
//        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
//        ArrayList<ArrayList<String>> contexts_lem = new ArrayList<ArrayList<String>>();
//        for (int i = 0; i < contexts.size(); i++) {
//            Annotation document = new Annotation(contexts.get(i));
//            ArrayList<String> tokens = new ArrayList<String>();
//            // run all Annotators on this text
//            pipeline.annotate(document);
//
//            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
//
//            for (CoreMap sentence : sentences) {
//                // traversing the words in the current sentence
//                // a CoreLabel is a CoreMap with additional token-specific methods
//                for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//                    // this is the lemma of the token
//                    String lemma = token.get(LemmaAnnotation.class);
//                    tokens.add(lemma);
//                }
//            }
//            contexts_lem.add(tokens);
//        }
//
//        return contexts_lem;
//    }
}
