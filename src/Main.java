
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetConfiguration;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetIDRelation;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
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
import java.util.Set;

public class Main {

    static HashMap<String, String> lemmaMap;
    static BabelNet bn;
    static boolean relations, rescaling;
    static BabelPointer kindOfRelations;

    public static void main(String[] args) {
        Main m = new Main();
        bn = BabelNet.getInstance();
        lemmaMap = getLemmaMap();
        relations = true;
        kindOfRelations = BabelPointer.HYPERNYM;
        rescaling = true;

        //Variabile che conterrá le configurazioni base di Babelnet
        BabelNetConfiguration conf = BabelNetConfiguration.getInstance();
        conf.setConfigurationFile(new File("config/babelnet.properties"));

        //array di contesti per la parola pianta e testa
        String[] contestiPianta = {
            "La pianta dell'alloggio è disponibile in ufficio, accanto all'appartamento; dal disegno è possibile cogliere i dettagli dell'architettura dello stabile, sulla distribuzione dei vani e la disposizione di porte e finestre.",
            "I platani sono piante ad alto fusto, organismi viventi: non ha senso toglierli per fare posto a un parcheggio."};
        String[] contestiTesta = {
            "Si tratta di un uomo facilmente riconoscibile: ha una testa piccola, gli occhi sporgenti, naso adunco e piccole orecchie a sventola.",
            "Come per tutte le cose, ci vorrebbe un po' di testa, un minimo di ragione, una punta di cervello, per non prendere decisioni fuori dal senso dell'intelletto."};

        String[] words = {"pianta", "testa"};

        //aggiunta dei contesti pianta nella lista principale
        ArrayList<String> lista_contesti_pianta = new ArrayList<>();
        lista_contesti_pianta.addAll(Arrays.asList(contestiPianta));
        //aggiunta dei contesti testa nella lista principale
        ArrayList<String> lista_contesti_testa = new ArrayList<>();
        lista_contesti_testa.addAll(Arrays.asList(contestiTesta));

        //rimuoviamo le stopwords a contesti pianta
        ArrayList<ArrayList<String>> contesti_pianta_noStopwords = new ArrayList<>();
        for (String contesto_pianta : lista_contesti_pianta) {
            contesti_pianta_noStopwords.add(removeStopwordsFromContexts(contesto_pianta));
        }

        ArrayList<ArrayList<String>> lista_lemmi_contesti_pianta = getLemsFromContexts(contesti_pianta_noStopwords);

        //rimuoviamo le stopwords a contesti testa
        ArrayList<ArrayList<String>> contesti_testa_noStopwords = new ArrayList<>();
        for (String contesto_testa : lista_contesti_testa) {
            contesti_testa_noStopwords.add(removeStopwordsFromContexts(contesto_testa));
        }

        ArrayList<ArrayList<String>> lista_lemmi_contesti_testa = getLemsFromContexts(contesti_testa_noStopwords);

        ArrayList<BabelSynset> lista_synset_parole = getSynsetsFromWord(words[0]);

        BabelSynsetID id;

        for (int i = 0; i < contestiPianta.length; i++) {
            int max_overlap = 0;
            int index = 0;
            int num_glosses = 0;
            for (int j = 0; j < lista_synset_parole.size(); j++) {
                int overlap = 0;
                ArrayList<BabelGloss> glosse_parola = (ArrayList<BabelGloss>) lista_synset_parole.get(j).getGlosses(Language.IT);
                if (relations) {
                    ArrayList<BabelSynsetIDRelation> lista_relazioni = (ArrayList<BabelSynsetIDRelation>) lista_synset_parole.get(j).getEdges(kindOfRelations);
                    for (BabelSynsetIDRelation relazione : lista_relazioni) {
                        id = relazione.getBabelSynsetIDTarget();
                        glosse_parola.addAll(bn.getSynset(id).getGlosses(Language.IT));
                    }
                }
                ArrayList<String> lista_string_glossa = new ArrayList<>();
                ArrayList<ArrayList<String>> lista_glosse_noStopword;
                for (BabelGloss glossa_parola : glosse_parola) {
                    lista_string_glossa.add(glossa_parola.getGloss());
                }
                lista_glosse_noStopword = removeStopwordsFromList(lista_string_glossa);
                lista_glosse_noStopword = getLemsFromContexts(lista_glosse_noStopword);
                for (ArrayList<String> lista_glossa_noStopword : lista_glosse_noStopword) {
                    overlap += computer_overlap(lista_lemmi_contesti_pianta.get(i), lista_glossa_noStopword);
                }
                if (rescaling && lista_glosse_noStopword.size() > 0) {
                    overlap /= lista_glosse_noStopword.size();
                }
                if (overlap > max_overlap) {
                    max_overlap = overlap;
                    index = j;
                    num_glosses = lista_glosse_noStopword.size();

                }
            }
            System.out.println(words[0] + " in the context '" + contestiPianta[i]
                    + "' means '" + lista_synset_parole.get(index) + "' thanks to an overlap equals"
                    + " to " + max_overlap + " and a number of glosses equal " + num_glosses);
        }

        lista_synset_parole = getSynsetsFromWord(words[1]);

        for (int i = 0; i < contestiTesta.length; i++) {
            int max_overlap = 0;
            int index = 0;
            int num_glosses = 0;
            for (int j = 0; j < lista_synset_parole.size(); j++) {
                int overlap = 0;
                ArrayList<BabelGloss> glosse_parola = (ArrayList<BabelGloss>) lista_synset_parole.get(j).getGlosses(Language.IT);
                if (relations) {
                    ArrayList<BabelSynsetIDRelation> lista_relazioni = (ArrayList<BabelSynsetIDRelation>) lista_synset_parole.get(j).getEdges(kindOfRelations);
                    for (int k = 0; k < lista_relazioni.size(); k++) {
                        id = lista_relazioni.get(k).getBabelSynsetIDTarget();
                        glosse_parola.addAll(bn.getSynset(id).getGlosses(Language.IT));
                    }
                }
                ArrayList<String> lista_string_glossa = new ArrayList<>();
                ArrayList<ArrayList<String>> lista_glosse_noStopword = new ArrayList<>();
                for (int z = 0; z < glosse_parola.size(); z++) {
                    lista_string_glossa.add(glosse_parola.get(z).getGloss());
                }
                lista_glosse_noStopword = removeStopwordsFromList(lista_string_glossa);
                lista_glosse_noStopword = getLemsFromContexts(lista_glosse_noStopword);
                for (int x = 0; x < lista_glosse_noStopword.size(); x++) {
                    overlap += computer_overlap(lista_lemmi_contesti_testa.get(i), lista_glosse_noStopword.get(x));
                }
                if (rescaling && lista_glosse_noStopword.size() > 0) {
                    overlap /= lista_glosse_noStopword.size();
                }
                if (overlap > max_overlap) {
                    max_overlap = overlap;
                    index = j;
                    num_glosses = lista_glosse_noStopword.size();
                }
            }
            System.out.println(words[1] + " in the context '" + contestiTesta[i]
                    + "' means '" + lista_synset_parole.get(index) + "' thanks to an overlap equal "
                    + " to " + max_overlap + " and a number of glosses equal " + num_glosses);
        }

    }

    private static int computer_overlap(ArrayList<String> context,
            ArrayList<String> gloss) {
        int count = 0;
        for (String token_context : context) {
            for (String token_gloss : gloss) {
                if (token_context.equalsIgnoreCase(token_gloss)) {
                    count++;
                }
            }
        }
        return count;
    }

    public static Set<String> getStopwords() {
        Set<String> stopWords = new LinkedHashSet<>();
        BufferedReader SW;
        try {
            String line;
            SW = new BufferedReader(new FileReader("src/stopwords.txt"));
            while ((line = SW.readLine()) != null) {
                if (!line.contains("|") && !line.equals("")) {
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
            String clean_word = word.replaceAll("[ \\p{Punct}]", " ");
            String[] tokens = clean_word.split("[\\s]+");
            for (String token : tokens) {
                if (!stopwords.contains(token.toLowerCase())) {
                    array_tokens.add(token);
                }
            }
        }
        return array_tokens;
    }

    public static ArrayList<ArrayList<String>> removeStopwordsFromList(ArrayList<String> lista) {
        ArrayList<ArrayList<String>> lista_stopwords_rimosse = new ArrayList<>();
        for (String word : lista) {
            lista_stopwords_rimosse.add(removeStopwordsFromContexts(word));
        }
        return lista_stopwords_rimosse;
    }

    public static HashMap<String, String> getLemmaMap() {
        HashMap<String, String> map = new HashMap<>();
        BufferedReader SW;
        try {
            String line;
            SW = new BufferedReader(new FileReader("src/morphit/morph-it_048.txt"));
            while ((line = SW.readLine()) != null) {
                if (!line.contains("|") && !line.equals("")) {
                    String[] row = line.split("[\\s]+");
                    map.put(row[0], row[1]);
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

        return map;
    }

    public static ArrayList<ArrayList<String>> getLemsFromContexts(ArrayList<ArrayList<String>> lems) {
        ArrayList<ArrayList<String>> lista_lemmi_contesti = new ArrayList<>();

        for (ArrayList<String> lem : lems) {
            ArrayList<String> lemmi_contesto = new ArrayList<>();
            for (int j = 0; j < lem.size(); j++) {
                String lemma = lemmaMap.get(lem.get(j));
                if (lemma != null) {
                    lemmi_contesto.add(lemma);
                } else {
                    lemmi_contesto.add(lem.get(j));
                }
            }
            lista_lemmi_contesti.add(lemmi_contesto);
        }
        return lista_lemmi_contesti;
    }

    public static ArrayList<BabelSynset> getSynsetsFromWord(String word) {

        ArrayList<BabelSynset> lista_Synset;
        lista_Synset = (ArrayList<BabelSynset>) bn.getSynsets(Language.IT, word);

        return lista_Synset;
    }
}
