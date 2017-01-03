import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.util.CoreMap;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Utilities;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.lang.Character.isLetter;


public class Main {

    public static File currentFile;
    public static File dictionaryFile;
    public static Map<String, Integer> dictionary;
    public static JTextArea textArea;
    public static long wordCount = 0;
    public static String selectedText;
    public static boolean isCreated = false;
    public static String stringToTag = "";
    public static List<HasWord> wordsToTag;
    public static String text;
    public static String newTag;
    public static File taggedFile;
    public static Map<String, ArrayList<String>> tagsForEachWord;
    public static Map<String, String> allTags;
    public static Map<String, String> allLemmas;
    public static Map<Pair<String, String>, Integer> tagPairs;
    public static Map<Pair<String, String>, Integer> tagWordAmount;
    public static Map<String, Integer> tagsAmount;
    public static Map<Pair<String, String>, Integer> currentTagPairs;
    public static Map<Pair<String, String>, Integer> currentTagWordAmount;
    public static Map<String, Integer> currentTagsAmount;
    public static boolean isDictionaryCreated;
    public static boolean isFileOpened;
    public static boolean isTextTagged;

    public static void saveStats(){
        File statTagPairs = new File("statTagPairs.txt");
        File statTagWordAmount = new File("statTagWordAmount.txt");
        File statTagsAmount = new File("statTagsAmount.txt");

        try {
            if (!statTagPairs.exists())
                statTagPairs.createNewFile();
            if (!statTagWordAmount.exists())
                statTagWordAmount.createNewFile();
            if (!statTagsAmount.exists())
                statTagsAmount.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(statTagPairs));
            for (Map.Entry<Pair<String, String>, Integer> entry : tagPairs.entrySet()) {
                bufferedWriter.write(entry.getValue() + "\t" + entry.getKey().getKey() + "\t" +
                        entry.getKey().getValue() + "\n");
            }
            bufferedWriter.close();
            bufferedWriter = new BufferedWriter(new FileWriter(statTagWordAmount));
            for (Map.Entry<Pair<String, String>, Integer> entry : tagWordAmount.entrySet()) {
                bufferedWriter.write(entry.getValue() + "\t" + entry.getKey().getKey() + "\t" +
                        entry.getKey().getValue() + "\n");
            }
            bufferedWriter.close();
            bufferedWriter = new BufferedWriter(new FileWriter(statTagsAmount));
            for (Map.Entry<String, Integer> entry : tagsAmount.entrySet()) {
                bufferedWriter.write(entry.getValue() + "\t" + entry.getKey() + "\n");
            }
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadDictionary() {
        File f = new File("dictionary.txt");
        if(f.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(f));
                String s = reader.readLine();
                while (s != null && !s.equals("")) {
                    String[] parts = s.split("\t");
                    dictionary.put(parts[2], Integer.parseInt(parts[0]));
                    String[] parts2 = parts[3].split("  ");
                    tagsForEachWord.put(parts[2], new ArrayList<>());
                    for (String str : parts2)
                        tagsForEachWord.get(parts[2]).add(str);
                    s = reader.readLine();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static void loadStats() {
        File statTagPairs = new File("statTagPairs.txt");
        File statTagWordAmount = new File("statTagWordAmount.txt");
        File statTagsAmount = new File("statTagsAmount.txt");

        BufferedReader reader = null;
        try {
            if (statTagPairs.exists()) {
                reader = new BufferedReader(new FileReader(statTagPairs));
                String s = reader.readLine();
                while (s != null && !s.equals("")) {
                    String[] parts = s.split("\t");
                    tagPairs.put(new Pair<String, String>(parts[1], parts[2]), Integer.parseInt(parts[0]));
                    s = reader.readLine();
                }
            }
            if (statTagWordAmount.exists()) {
                reader = new BufferedReader(new FileReader(statTagWordAmount));
                String s = reader.readLine();
                while (s != null && !s.equals("")) {
                    String[] parts = s.split("\t");
                    tagWordAmount.put(new Pair<String, String>(parts[1], parts[2]), Integer.parseInt(parts[0]));
                    s = reader.readLine();
                }
            }
            if (statTagsAmount.exists()) {
                reader = new BufferedReader(new FileReader(statTagsAmount));
                String s = reader.readLine();
                while (s != null && !s.equals("")) {
                    String[] parts = s.split("\t");
                    tagsAmount.put(parts[1], Integer.parseInt(parts[0]));
                    s = reader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void sortByAlphabet(List entries) {
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return a.getKey().compareTo(b.getKey());
            }
        });
    }
    public static void sortAscending(List entries) {
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return a.getValue().compareTo(b.getValue());
            }
        });
    }
    public static void sortDescending(List entries) {
        Collections.sort(entries, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> a, Map.Entry<String, Integer> b) {
                return -1 * a.getValue().compareTo(b.getValue());
            }
        });
    }

    public static Map sort(Map<String, Integer> hashtable, int orderSort) {
        LinkedHashMap map = copy(hashtable);
        List<Map.Entry<String, Integer>> entries =
                new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
        if (orderSort == 0)
            sortByAlphabet(entries);
        else if (orderSort == 1)
            sortAscending(entries);
        else
            sortDescending(entries);
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Map.Entry<String, Integer> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        dictionary = sortedMap;
        saveDictionary(dictionary);
        return sortedMap;
    }

    public static LinkedHashMap copy(Map<String, Integer> hashtable) {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>(hashtable);
        return result;
    }

    public static String edit(String word){
        String editedWord = word;
        boolean letterFlag = false;
        for (char c : editedWord.toCharArray()){
            if (isLetter(c)){
                letterFlag = true;
                break;
            }
        }
        if (!letterFlag)
            return "";
        while (!isLetter(editedWord.charAt(0)))
            editedWord = editedWord.substring(1);
        while (editedWord.length() != 0 &&
                !isLetter(editedWord.charAt(editedWord.length() - 1)))
            editedWord = editedWord.substring(0, editedWord.length() - 1);
        return editedWord;
    }

    public static Hashtable tokenize(FileReader reader) throws IOException {
        Hashtable<String, Integer> hashtable = new Hashtable<>();
        allLemmas = new LinkedHashMap<>();
        try (BufferedReader br = new BufferedReader(reader)) {
            String s;
            String previousLine = "";
            while ((s = br.readLine()) != null) {
                StringTokenizer st = new StringTokenizer(s, " \t\n\r,.?!;&:*");
                if (previousLine.length() != 0 && previousLine.charAt(previousLine.length() - 1) == '-' && s.charAt(0) != ' ') {
                    StringTokenizer tokenizer = new StringTokenizer(previousLine, " \t\n\r,.?!;&:*");
                    String wordWithDash = "";
                    while (tokenizer.hasMoreTokens()) {
                        wordWithDash = tokenizer.nextToken().toLowerCase();
                    }
                    wordWithDash += st.nextToken();
                    wordWithDash = wordWithDash.replaceAll("-", "");
                    if (hashtable.containsKey(wordWithDash)) {
                        hashtable.put(wordWithDash, hashtable.get(wordWithDash) + 1);
                    } else
                        hashtable.put(wordWithDash, 1);
                }
                String nextWord = "";
                while (st.hasMoreTokens()) {
                    nextWord = st.nextToken().toLowerCase();
                    if (!nextWord.equals(""))
                        nextWord = edit(nextWord);
                    if (!nextWord.equals("")) {
                        wordCount++;
                        if (hashtable.containsKey(nextWord)) {
                            hashtable.put(nextWord, hashtable.get(nextWord) + 1);
                        } else
                            hashtable.put(nextWord, 1);
                    }
                }
                previousLine = s;
            }
        } catch (IOException ex) {

            System.out.println(ex.getMessage());
        }

        //dictionary = hashtable;
        dictionary.putAll(hashtable);
        return hashtable;
    }

    private static void tagText() {
        MaxentTagger tagger =  new MaxentTagger("tagger/english-left3words-distsim.tagger");
        List<String> originalText = new ArrayList<>();

        try {
            TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(),
                    "untokenizable=noneKeep, invertible=true");
            BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(currentFile)));
            StringTokenizer tokenizer = new StringTokenizer(text, ".?!");
            while (tokenizer.hasMoreTokens()) {
                originalText.add(tokenizer.nextToken());
            }
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(taggedFile)));

            DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor(r);
            documentPreprocessor.setTokenizerFactory(ptbTokenizerFactory);


            int index = 0;
            for (List<HasWord> sentence : documentPreprocessor) {
                //System.out.println(sentence);
                List<TaggedWord> tSentence = tagger.tagSentence(sentence);
                TaggedWord previousWord = null;

                int sentenceStart = textArea.getText().indexOf(originalText.get(index));

                //String currentText = textArea.getText();
                for (TaggedWord word : tSentence) {

                    boolean isBad = false;
                    if (word.tag().equals(word.word())) {
                        previousWord = null;
                        continue;
                    }
                    for (char c : word.tag().toCharArray()) {
                        if (!isLetter(c)) {
                            previousWord = null;
                            isBad = true;
                            break;
                        }
                    }
                    if (isBad)
                        continue;
                    if (previousWord != null) {
                        if (!word.word().equals(word.tag()) && !previousWord.word().equals(previousWord.tag())) {
                            if (!tagPairs.containsKey(new Pair<>(previousWord.tag(), word.tag())))
                                tagPairs.put(new Pair<>(previousWord.tag(), word.tag()), 1);
                            else tagPairs.replace(new Pair<>(previousWord.tag(), word.tag()),
                                    tagPairs.get(new Pair<>(previousWord.tag(), word.tag())) + 1);
                        }
                    }
                    if (tagsAmount.containsKey(word.tag()))
                        tagsAmount.replace(word.tag(), tagsAmount.get(word.tag()) + 1);


                    if (tagsForEachWord.containsKey(word.word())) {
                        boolean flag = false;
                        for (String tag : tagsForEachWord.get(word.word())) {
                            if (tag.equals(word.tag()))
                                flag = true;
                        }
                        if (!flag)
                            tagsForEachWord.get(word.word()).add(word.tag());
                    } else {
                        tagsForEachWord.put(word.word(), new ArrayList<String>());
                        tagsForEachWord.get(word.word()).add(word.tag());
                    }
                    if (tagWordAmount.containsKey(new Pair(word.word(), word.tag()))) {
                        tagWordAmount.replace(new Pair(word.word(), word.tag()),
                                tagWordAmount.get(new Pair(word.word(), word.tag())) + 1);
                    } else {
                        tagWordAmount.put(new Pair(word.word(), word.tag()), 1);
                    }
                    previousWord = word;

                    int start = textArea.getText().substring(sentenceStart).indexOf(word.word());

                    if (start >= 0 && word.word().length() > 0)
                        textArea.replaceRange(word.word() + "_" + word.tag(), start + sentenceStart, start + sentenceStart
                                + word.word().length());
                    sentenceStart += word.word().length() + start + word.tag().length() - 1;
                }
                index++;
            }
            w.write(textArea.getText());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeTags(Map<String, Integer> hashtable){

        dictionaryFile = new File("dictionary.txt");
        try {
            if (!dictionaryFile.exists())
                dictionaryFile.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dictionaryFile));

            for (Map.Entry<String, Integer> entry : hashtable.entrySet()) {
                bufferedWriter.write(entry.getValue() + "\t-\t" + entry.getKey() + "\t");
                for (String tag : tagsForEachWord.get(entry.getKey()))
                    bufferedWriter.write( tag + "  ");
                bufferedWriter.write("\n");
            }

            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void tag(Map<String, Integer> hashtable){
        MaxentTagger tagger =  new MaxentTagger("tagger/english-left3words-distsim.tagger");
        List<TaggedWord> taggedSentence = tagger.tagSentence(wordsToTag);

        dictionaryFile = new File("dictionary.txt");
        try {
            if (!dictionaryFile.exists())
                dictionaryFile.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dictionaryFile));
            Map<String, Integer> taggedDictionary = new LinkedHashMap<>();

            int index = 0;
            for (Map.Entry<String, Integer> entry : hashtable.entrySet()) {
                taggedDictionary.put(taggedSentence.get(index).word(), entry.getValue());
                tagsForEachWord.put(taggedSentence.get(index).word(), new ArrayList<>());
                tagsForEachWord.get(taggedSentence.get(index).word()).add(taggedSentence.get(index).tag());
                bufferedWriter.write(entry.getValue() + "\t-\t" + taggedSentence.get(index).word() + "\t" +
                        taggedSentence.get(index).tag() + "\n");

                String word = taggedSentence.get(index).word();
                String tag = taggedSentence.get(index).tag();
                if (tagWordAmount.containsKey(new Pair(word, tag))){
                    tagWordAmount.replace(new Pair(word, tag),
                            tagWordAmount.get(new Pair(word, tag)) + 1);
                }
                else {
                    tagWordAmount.put(new Pair(word, tag), 1);
                }

                index++;
            }

            dictionary.clear();
            //dictionary = taggedDictionary;
            dictionary.putAll(taggedDictionary);
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getAllTags() {
        StanfordCoreNLP pipeline;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
        Annotation document = null;
        try {
            document = new Annotation(read(currentFile.getAbsolutePath()));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pipeline.annotate(document);
        List<CoreMap> sentences =
                document.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            for (CoreLabel word : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (!word.word().equals(""))
                    word.setWord(edit(word.word()));
                if (!word.word().equals("")) {
                    if (tagWordAmount.containsKey(new Pair(word.word(), word.tag()))){
                        tagWordAmount.replace(new Pair(word.word(), word.tag()),
                                tagWordAmount.get(new Pair(word.word(), word.tag())) + 1);
                    }
                    else {
                        tagWordAmount.put(new Pair(word.word(), word.tag()), 1);
                    }

                    if (tagsForEachWord.containsKey(word.word())) {
                        boolean flag = false;
                        for (String tag : tagsForEachWord.get(word.word())) {
                            if (tag.equals(word.tag()))
                                flag = true;
                        }
                        if (!flag)
                            tagsForEachWord.get(word.word()).add(word.tag());
                    } else {
                        tagsForEachWord.put(word.word(), new ArrayList<>());
                        tagsForEachWord.get(word.word()).add(word.tag());
                    }
                }
            }
        }
    }

    public static void setTags2(String editingText){
        textArea.setEditable(true);
        if (editingText == null)
            return;
        if (!tagsForEachWord.containsKey(editingText)){
            JPanel panel = new JPanel();
            panel.setLocation(300, 300);
            panel.setMinimumSize(new Dimension(500, 500));
            JPanel subPanel = new JPanel();

            subPanel.setLayout(new GridLayout(12, 3));
            ButtonGroup buttonGroup = new ButtonGroup();
            for (Map.Entry<String, String> entry : allTags.entrySet()) {
                JRadioButton button = new JRadioButton(entry.getKey() + " " + entry.getValue());
                buttonGroup.add(button);
                subPanel.add(button);
            }
            JButton saveButton = new JButton("Сохранить");

            JDialog jDialog = new JDialog();

            JTextField subTextField = new JTextField("Tag for word " + editingText);
            subTextField.setHorizontalAlignment(JTextField.CENTER);
            subTextField.setEditable(false);

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg) {
                    String tag = "";
                    for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
                        AbstractButton button = buttons.nextElement();

                        if (button.isSelected()) {
                            tag = button.getText();
                        }
                    }
                    int start = textArea.getText().indexOf(editingText);
                    if (start >= 0 && editingText.length() > 0) {
                        textArea.replaceRange(editingText + "_" + tag.substring(0, tag.indexOf("\t")), start, start
                                + editingText.length());

                    }

                    jDialog.setVisible(false);
                }
            });
            panel.setLayout(new BorderLayout());
            panel.add(subTextField, BorderLayout.NORTH);
            panel.add(subPanel, BorderLayout.CENTER);
            panel.add(saveButton, BorderLayout.SOUTH);


            jDialog.add(panel);
            jDialog.setLocation(300, 300);
            jDialog.setSize(900, 400);
            jDialog.setMinimumSize(new Dimension(900, 400));
            jDialog.setVisible(true);
        }
        else {
            if (tagsForEachWord.get(editingText).size() == 1) {
                int start = textArea.getText().indexOf(editingText);
                if (start >= 0 && editingText.length() > 0)
                    textArea.replaceRange(editingText + "_" + tagsForEachWord.get(editingText).get(0), start, start
                            + editingText.length());
            }
            else {

                if (editingText != null && editingText != "") {
                    JPanel panel = new JPanel();
                    panel.setLocation(500, 200);
                    panel.setMinimumSize(new Dimension(300, 300));
                    JPanel subPanel = new JPanel();

                    JTextField subTextField = new JTextField("Tag for word " + editingText);
                    subTextField.setHorizontalAlignment(JTextField.CENTER);
                    subTextField.setEditable(false);


                    ButtonGroup buttonGroup = new ButtonGroup();
                    int amount = 0;
                    for (Map.Entry<String, String> entry : allTags.entrySet()) {
                        if (tagsForEachWord.get(editingText).contains(entry.getKey())) {
                            JRadioButton button = new JRadioButton(entry.getKey() + " " + entry.getValue());
                            buttonGroup.add(button);
                            subPanel.add(button);
                            amount++;
                        }
                    }

                    subPanel.setLayout(new GridLayout(amount, 1));
                    panel.setSize(new Dimension(500, 100 * amount));
                    JButton saveButton = new JButton("Сохранить");

                    JDialog jDialog = new JDialog();


                    saveButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent arg) {
                            String tag = "";
                            for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements(); ) {
                                AbstractButton button = buttons.nextElement();

                                if (button.isSelected()) {
                                    tag = button.getText();
                                }
                            }
                            int start = textArea.getText().indexOf(editingText);
                            if (start >= 0 && editingText.length() > 0)
                                textArea.replaceRange(editingText + "_" + tag.substring(0, tag.indexOf("\t")), start, start
                                        + editingText.length());

                            jDialog.setVisible(false);
                        }
                    });
                    panel.setLayout(new BorderLayout());
                    panel.add(subTextField, BorderLayout.NORTH);
                    panel.add(subPanel, BorderLayout.CENTER);
                    panel.add(saveButton, BorderLayout.SOUTH);


                    jDialog.add(panel);
                    jDialog.setLocation(300, 300);
                    jDialog.setSize(900, 400);
                    jDialog.setMinimumSize(new Dimension(900, 400));
                    jDialog.setVisible(true);
                }
            }
        }

    }
    public static String setTags(String editingText) {
        textArea.setEditable(true);
        String tag1 = "";
        if (editingText != null  && editingText != "") {
            JPanel panel = new JPanel();
            panel.setLocation(300, 300);
            panel.setMinimumSize(new Dimension(500, 500));
            JPanel subPanel = new JPanel();

            subPanel.setLayout(new GridLayout(12, 3));
            ButtonGroup buttonGroup = new ButtonGroup();
            for (Map.Entry<String, String> entry : allTags.entrySet()) {
                JRadioButton button = new JRadioButton(entry.getKey() + " " + entry.getValue());
                buttonGroup.add(button);
                subPanel.add(button);
            }
            JButton saveButton = new JButton("Сохранить");

            JDialog jDialog = new JDialog();

            JTextField subTextField = new JTextField("Tag for word " + editingText);
            subTextField.setHorizontalAlignment(JTextField.CENTER);
            subTextField.setEditable(false);

            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent arg) {

                    String tag = "";
                    for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
                        AbstractButton button = buttons.nextElement();

                        if (button.isSelected()) {
                            tag = button.getText();
                        }
                    }
                    int start = textArea.getText().indexOf(editingText);
                    if (start >= 0 && editingText.length() > 0)
                        textArea.replaceRange(editingText + "_" + tag.substring(0, tag.indexOf("\t")), start, start
                                + editingText.length());
                    newTag = tag;
                    jDialog.setVisible(false);
                }
            });
            panel.setLayout(new BorderLayout());
            panel.add(subTextField, BorderLayout.NORTH);
            panel.add(subPanel, BorderLayout.CENTER);
            panel.add(saveButton, BorderLayout.SOUTH);


            jDialog.add(panel);
            jDialog.setLocation(300, 300);
            jDialog.setSize(900, 400);
            jDialog.setMinimumSize(new Dimension(900, 400));
            jDialog.setVisible(true);

        }
        tag1 = newTag;
        return tag1;
    }

    public static void lemmatize() {
        StanfordCoreNLP pipeline;
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma");
        pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
        List<CoreMap> sentences =
                document.get(CoreAnnotations.SentencesAnnotation.class);
        List<String> lemmas = new LinkedList<>();
        List<CoreLabel> lemmasCoreLabels = new LinkedList<>();
        for (CoreMap sentence : sentences) {
            for (CoreLabel word : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                if (!word.word().equals(""))
                    word.setWord(edit(word.word()));
                if (!word.word().equals("")) {
                    lemmas.add(word.get(CoreAnnotations.LemmaAnnotation.class));

                    lemmasCoreLabels.add(word);
                    allLemmas.put(word.word(), word.get(CoreAnnotations.LemmaAnnotation.class));
                }
            }
        }
        dictionaryFile = new File("dictionary.txt");
        try {
            if (!dictionaryFile.exists())
                dictionaryFile.createNewFile();

            Map<String, Integer> taggedDictionary = new LinkedHashMap<>();

            LinkedHashMap<String, Integer> hashtable = (LinkedHashMap<String, Integer>) dictionary;
            for (int i = 0; i < lemmas.size(); i++) {
                String element = lemmas.get(i);
                boolean isBad = false;
                if (element == null)
                    continue;
                for (Map.Entry<String, String> entry : allTags.entrySet()) {
                    if (element.equals(entry.getKey()) || element.equals(entry.getKey().toLowerCase())){
                        isBad = true;
                        allLemmas.remove(element);
                        allLemmas.remove(element.toLowerCase());
                        allLemmas.remove(element.toUpperCase());
                        break;
                    }
                }
                if (isBad)
                    continue;;
                CoreLabel coreLabel = lemmasCoreLabels.get(i);
                if (!element.equals(""))
                    element = edit(element);
                if (!element.equals("")) {
                    {
                        if (!hashtable.containsKey(element) && !hashtable.containsKey(element.toLowerCase())
                                && !hashtable.containsKey(element.toUpperCase())) {
                            taggedDictionary.put(element, 1);
                            tagsForEachWord.put(element, new ArrayList<>());
                            wordCount++;
                            tagsForEachWord.get(element).add(coreLabel.get(CoreAnnotations.PartOfSpeechAnnotation.class));
                            allLemmas.put(element, coreLabel.get(CoreAnnotations.LemmaAnnotation.class));
                        }
                    }
                }
            }

            dictionary.putAll(taggedDictionary);
            writeTags(dictionary);
            //saveDictionary(dictionary);
            printDictionary();

            dictionary.putAll(taggedDictionary);
//            saveDictionary(dictionary);
//            tag(dictionary);
//            printDictionary();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void groupWords() {
        LinkedHashMap<String, String> hashtable = (LinkedHashMap<String, String>) allLemmas;

        LinkedHashMap<String, ArrayList<String>> groups = new LinkedHashMap<>();

        for (Map.Entry<String, String> entry : hashtable.entrySet()) {
            if (!groups.containsKey(entry.getValue()))
                groups.put(entry.getValue(), new ArrayList<>());
            if (!groups.get(entry.getValue()).contains(entry.getValue()))
                groups.get(entry.getValue()).add(entry.getKey());
        }
        textArea.setText(null);
        for (Map.Entry<String, ArrayList<String>> entry : groups.entrySet()) {
            textArea.append(entry.getKey() + " \t-\t");
            for (String word : entry.getValue())
                textArea.append(word + "  ");
            textArea.append("\n");
        }
    }

    public static void showStats() {


        JDialog jDialog = new JDialog();

        JPanel panel = new JPanel();

        String[] columnNames = {"Тэг", "Расшифровка", "Кол-во"};
        DefaultTableModel model = new DefaultTableModel(columnNames, tagsAmount.size()) {
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        JTable currentTable = new JTable(model);
        RowSorter<TableModel> sorter = new TableRowSorter<>(model);
        currentTable.setRowSorter(sorter);
        currentTable.setVisible(true);
        currentTable.setEnabled(false);

        JScrollPane tableScrollPane = new JScrollPane();
        tableScrollPane.setViewportView(currentTable);

        int index = 0;
        for (Map.Entry<String, Integer> entry : tagsAmount.entrySet()) {
            currentTable.setValueAt(entry.getKey(), index, 0);
            currentTable.setValueAt(allTags.get(entry.getKey()), index, 1);
            currentTable.setValueAt(entry.getValue(), index, 2);
            index++;
        }


        //////////

        int count = 0;
        for (Map.Entry<String, ArrayList<String>> entry : tagsForEachWord.entrySet()) {
            for (String tag : entry.getValue()) {
                count++;
            }
        }
        String[] columnNames2 = {"Слово", "Тэг", "Расшифровка", "Кол-во"};
        DefaultTableModel model2 = new DefaultTableModel(columnNames2, count) {
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };


        JTable currentTable2 = new JTable(model2);
        RowSorter<TableModel> sorter2 = new TableRowSorter<>(model2);
        currentTable2.setRowSorter(sorter2);
        currentTable2.setVisible(true);
        currentTable2.setEnabled(false);

        JScrollPane tableScrollPane2 = new JScrollPane();
        tableScrollPane2.setViewportView(currentTable2);

        index = 0;
        for (Map.Entry<String, ArrayList<String>> entry : tagsForEachWord.entrySet()) {
            for (String tag : entry.getValue()){
                currentTable2.setValueAt(entry.getKey(), index, 0);
                currentTable2.setValueAt(tag, index, 1);
                currentTable2.setValueAt(allTags.get(tag), index, 2);
                currentTable2.setValueAt(tagWordAmount.get(new Pair<>(entry.getKey(), tag)), index, 3);
                index++;
            }
        }

        /////////////
        String[] columnNames3 = {"Тэг", "Расшифровка", "Тэг", "Расшифровка", "Кол-во"};
        DefaultTableModel model3 = new DefaultTableModel(columnNames3, tagPairs.size()) {
            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return String.class;
                    case 3:
                        return String.class;
                    case 4:
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
        };

        JTable currentTable3 = new JTable(model3);
        RowSorter<TableModel> sorter3 = new TableRowSorter<>(model3);
        currentTable3.setRowSorter(sorter3);
        currentTable3.setVisible(true);
        currentTable3.setEnabled(false);

        JScrollPane tableScrollPane3 = new JScrollPane();
        tableScrollPane3.setViewportView(currentTable3);

        index = 0;
        for (Map.Entry<Pair<String, String>, Integer> entry : tagPairs.entrySet()) {
            currentTable3.setValueAt(entry.getKey().getKey(), index, 0);
            currentTable3.setValueAt(allTags.get(entry.getKey().getKey()), index, 1);
            currentTable3.setValueAt(entry.getKey().getValue(), index, 2);
            currentTable3.setValueAt(allTags.get(entry.getKey().getValue()), index, 3);
            currentTable3.setValueAt(entry.getValue(), index, 4);
            index++;

        }

        ////////////////////

        JPanel buttonsPanel = new JPanel();
        JButton button1 = new JButton("Назад");
        button1.setVisible(false);
        JButton button2 = new JButton("Вперед");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (tableScrollPane2.isVisible()){
                    tableScrollPane2.setVisible(false);
                    tableScrollPane.setVisible(true);
                    button1.setVisible(false);
                }
                else {
                    tableScrollPane3.setVisible(false);
                    tableScrollPane2.setVisible(true);
                    button2.setVisible(true);
                }
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (tableScrollPane.isVisible()){
                    tableScrollPane.setVisible(false);
                    tableScrollPane2.setVisible(true);
                    button1.setVisible(true);
                }
                else if (tableScrollPane2.isVisible()){
                    tableScrollPane2.setVisible(false);
                    tableScrollPane3.setVisible(true);
                    button2.setVisible(false);
                }
            }
        });

        buttonsPanel.add(button1);
        buttonsPanel.add(button2);

        panel.add(tableScrollPane);
        panel.add(tableScrollPane2);
        panel.add(tableScrollPane3);

        tableScrollPane.setPreferredSize(new Dimension(800, 500));
        tableScrollPane2.setPreferredSize(new Dimension(800, 500));
        tableScrollPane3.setPreferredSize(new Dimension(800, 500));

        tableScrollPane2.setVisible(false);
        tableScrollPane3.setVisible(false);

        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));

        container.add(panel);
        container.add(buttonsPanel);

        jDialog.add(container);
        jDialog.setLocation(200, 50);
        jDialog.setSize(800, 600);
        jDialog.setMinimumSize(new Dimension(800, 600));
        jDialog.setPreferredSize(new Dimension(800, 600));
        jDialog.setMaximumSize(new Dimension(800, 600));
        jDialog.setResizable(false);

        jDialog.setVisible(true);

        saveStats();
    }


    public static void saveDictionary(Map<String, Integer> hashtable) {
        dictionaryFile = new File("dictionary.txt");
        try {
            if (!dictionaryFile.exists())
                dictionaryFile.createNewFile();

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(dictionaryFile));

            wordsToTag = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : hashtable.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                bufferedWriter.write(value + "\t-\t" + key + "\n");
                stringToTag += key + " ";
                wordsToTag.add(new Word(key));
            }
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void printDictionary() {
        String filePath = dictionaryFile.getPath();
        textArea.setText(null);
        try {
            textArea.setText(null);
            textArea.append(read(filePath));
        } catch (java.io.FileNotFoundException e) {
            System.out.println("File not found");
        }
    }

    public static String read(String fileName) throws FileNotFoundException {
        textArea.setText(null);
        StringBuilder sb = new StringBuilder();
        File file = new File(fileName);
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            try {
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append("\n");
                }
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

    public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get (key);
            if (value != null && value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put (key, f);
        }
    }

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
            setUIFont(new javax.swing.plaf.FontUIResource("Serif", Font.PLAIN, 18));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        isDictionaryCreated = false;
        isFileOpened = false;
        isTextTagged = false;
        JFrame frame = new JFrame("Dictionary");
        frame.setVisible(true);
        frame.setSize(1200, 550);
        frame.setMinimumSize(new Dimension(1000, 550));
        frame.setLocation(100, 100);

        JPanel panel = new JPanel();
        JPanel subPanel = new JPanel();

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setVisible(true);

        JScrollPane scroll = new JScrollPane (textArea);
        JButton createDictionaryButton = new JButton("Создать словарь");
        createDictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                final JDialog d = new JDialog();
                JPanel p1 = new JPanel(new GridBagLayout());
                JLabel label = new JLabel("Пожалуйста, подождите ...");
                p1.add(label, new GridBagConstraints());
                d.getContentPane().add(p1);
                d.setSize(250, 100);
                d.setLocationRelativeTo(frame);
                d.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                d.setModal(true);

                SwingWorker<?, ?> worker = new SwingWorker<Void, Integer>() {
                    protected Void doInBackground() throws InterruptedException {
                        if (!isFileOpened) {
                            JOptionPane.showMessageDialog(frame,
                                    "Текст не выбран. Выберите файл",
                                    "Warning",
                                    JOptionPane.WARNING_MESSAGE);
                        } else {
                            try {
                                isDictionaryCreated = true;
                                stringToTag = "";
                                tokenize(new FileReader(currentFile));
                                isCreated = true;
                                saveDictionary(dictionary);
                                tag(dictionary);
                                getAllTags();
                                writeTags(dictionary);
                                printDictionary();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return null;
                    }

                    protected void done() {
                        label.setText("Создание успешно завершено.");
                         d.dispose();
                    }
                };
                worker.execute();
                d.setVisible(true);
            }
        });


        DefaultCaret caret = (DefaultCaret) textArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        JButton alphabetButton = new JButton("Сортировать по алфавиту");
        alphabetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    sort(dictionary, 0);
                    writeTags(dictionary);
                    printDictionary();
                }
            }
        });
        JButton ascendingButton = new JButton("Сортировать по  возрастанию");
        ascendingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    sort(dictionary, 1);
                    writeTags(dictionary);
                    printDictionary();
                }
            }
        });
        JButton descendingButton = new JButton("Сортировать по убыванию");
        descendingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    sort(dictionary, 2);
                    writeTags(dictionary);
                    printDictionary();
                }
            }
        });

        textArea.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }
                if (e.getClickCount() != 2) {
                    return;
                }
                int offset = textArea.viewToModel(e.getPoint());
                try {
                    int rowStart = Utilities.getRowStart(textArea, offset);
                    int rowEnd = Utilities.getRowEnd(textArea, offset);
                    String selectedLine = textArea.getText().substring(rowStart, rowEnd);
                    selectedText = selectedLine.substring(selectedLine.lastIndexOf(' ') + 1);

                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                }
            }
        });

        JButton editButton = new JButton("Редактировать словарь");
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    textArea.setEditable(true);
                    String editingText = textArea.getSelectedText();
                    String editingTextForField = editingText;

                    if (editingText != null && editingText != "") {
                        JPanel panel = new JPanel();
                        panel.setSize(400, 200);
                        panel.setLocation(300, 300);
                        panel.setMinimumSize(new Dimension(400, 200));

                        JTextArea textField = new JTextArea();
                        JButton saveButton = new JButton("Сохранить");
                        JDialog jDialog = new JDialog();

                        textField.setEditable(true);
                        textField.setText(editingTextForField);
                        textField.setEditable(true);

                        saveButton.addActionListener(new ActionListener() {
                            @Override
                            public void actionPerformed(ActionEvent arg) {
                                String newText = textField.getText();
                                if (dictionary.containsKey(newText))
                                    dictionary.put(newText, dictionary.get(newText) +
                                            dictionary.get(editingText));
                                else {
                                    dictionary.put(newText, dictionary.get(editingText));
                                    MaxentTagger tagger =  new MaxentTagger("tagger/english-left3words-distsim.tagger");
                                    String taggedNewText = tagger.tagString(newText);
                                    String newTag = taggedNewText.substring(taggedNewText.indexOf("_") + 1);
                                    if (newTag.charAt(newTag.length() - 1) == ' ')
                                        newTag = newTag.replaceAll(" ", "");
                                    tagsForEachWord.put(newText, new ArrayList<>());
                                    tagsForEachWord.get(newText).add(newTag);
                                    tagsAmount.replace(newTag, tagsAmount.get(newTag) + 1);
                                    for (String tag : tagsForEachWord.get(editingText)) {
                                        tagsAmount.replace(tag, tagsAmount.get(tag) - 1);
                                    }
                                }
                                dictionary.remove(editingText);
                                writeTags(dictionary);
                                printDictionary();
                                jDialog.setVisible(false);
                            }
                        });
                        panel.setLayout(new BorderLayout());
                        panel.add(textField, BorderLayout.CENTER);
                        panel.add(saveButton, BorderLayout.SOUTH);

                        jDialog.add(panel);
                        jDialog.setTitle("Редактирование слова");
                        jDialog.setLocation(300, 300);
                        jDialog.setMinimumSize(new Dimension(300, 100));
                        jDialog.setSize(new Dimension(300, 100));
                        jDialog.setVisible(true);
                    }
                }

            }
        });
        JButton deleteButton = new JButton("Удалить слово");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    textArea.setEditable(true);
                    String editingText = textArea.getSelectedText();

                    if (editingText != null && editingText != "") {
                        JPanel panel = new JPanel();
                        panel.setSize(400, 200);
                        panel.setLocation(300, 300);
                        panel.setMinimumSize(new Dimension(400, 200));

                        int result = JOptionPane.showConfirmDialog((Component) null, "Удалить слово " + editingText + " ?",
                                "Удаление слова", JOptionPane.OK_CANCEL_OPTION);

                        if (result == 0) {
                            dictionary.remove(editingText);
                            writeTags(dictionary);
                            printDictionary();
                            JOptionPane.showMessageDialog(frame,
                                    "Слово успешно удалено",
                                    "Success",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

        JButton paintButton = new JButton("Раскрасить текст");
        paintButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    isTextTagged = true;
                    textArea.setText(null);
                    try {
                        textArea.setText(read(currentFile.getAbsolutePath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    tagText();
                }
            }
        });

        JButton editTextButton = new JButton("Редактировать текст");
        editTextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else if (!isTextTagged) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не раскрашен. Раскрасьте текст",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    String editingText = textArea.getSelectedText();
                    if (editingText.charAt(0) == 65279)
                        editingText = editingText.substring(1);
                    int start = textArea.getText().indexOf(editingText);

                    String editingTag = editingText.substring(editingText.indexOf("_") + 1);
                    if (start >= 0 && editingText.length() > 0)
                        textArea.replaceRange(editingText.substring(0, editingText.indexOf("_")), start, start
                                + editingText.length());
                    editingText = editingText.substring(0, editingText.indexOf("_"));

                    String newTag = setTags(editingText);
                    System.out.println(newTag + "SSSSSSSSS");
                    if (newTag == null)
                        newTag = editingTag;
                    System.out.println(newTag + " DDDDD");
                    ArrayList<String> oldTags = tagsForEachWord.get(editingText);
                    ArrayList<String> newTags = new ArrayList<String>();
                    for(String oldTag : oldTags){
                        System.out.println(oldTag);
                        if (oldTag.equals(editingTag))
                        {
                            newTags.add(newTag);
                        }
                        else {
                            newTags.add(oldTag);
                        }
                    }
                    tagsForEachWord.replace(editingText, newTags);
                    writeTags(dictionary);

                }
            }
        });

        JButton addFormsButton = new JButton("Добавить формы");
        addFormsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else lemmatize();
            }
        });
        JButton tagButton = new JButton("Выбрать теги");
        tagButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else setTags(textArea.getSelectedText());
            }
        });

        JButton tag2Button = new JButton("Выбрать теги полуавтоматически");
        tag2Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else setTags2(textArea.getSelectedText());
            }
        });

        JButton showButton = new JButton("Показать исходный текст");
        showButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    textArea.setText(null);
                    try {
                        textArea.setText(read(currentFile.getAbsolutePath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        JButton showTaggedButton = new JButton("Показать раскрашенный текст");
        showTaggedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isFileOpened) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не выбран. Выберите файл",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else if (!isTextTagged) {
                    JOptionPane.showMessageDialog(frame,
                            "Текст не раскрашен. Раскрасьте текст",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    textArea.setText(null);
                    try {
                        textArea.setText(read(taggedFile.getAbsolutePath()));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        JButton groupButton = new JButton("Показать группы слов");
        groupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    lemmatize();
                    groupWords();
                }
            }
        });

        JButton dictionaryButton = new JButton("Показать словарь");
        dictionaryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    printDictionary();
                }
            }
        });

        JButton statisticsButton = new JButton("Показать статистику");
        statisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else showStats();
            }
        });

        JButton clearButton = new JButton("Очистить словарь");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                if (!isDictionaryCreated) {
                    JOptionPane.showMessageDialog(frame,
                            "Словарь не создан. Создайте словарь",
                            "Warning",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    int result = JOptionPane.showConfirmDialog((Component) null, "Очистить словарь ?",
                            "Очистка словаря", JOptionPane.OK_CANCEL_OPTION);

                    if (result == 0) {
                        dictionary.clear();
                        writeTags(dictionary);
                        printDictionary();
                        JOptionPane.showMessageDialog(frame,
                                "Словарь успешно очищен",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }

                }
            }
        });

        subPanel.setLayout(new GridLayout(16, 1));
        subPanel.add(alphabetButton);
        subPanel.add(ascendingButton);
        subPanel.add(descendingButton);
        subPanel.add(editButton);
        subPanel.add(deleteButton);
        subPanel.add(paintButton);
        subPanel.add(editTextButton);
        subPanel.add(addFormsButton);
        subPanel.add(tagButton);
        subPanel.add(tag2Button);
        subPanel.add(showButton);
        subPanel.add(showTaggedButton);
        subPanel.add(groupButton);
        subPanel.add(dictionaryButton);
        subPanel.add(statisticsButton);
        subPanel.add(clearButton);

        final DefaultListModel listModel = new DefaultListModel();

        final JList list = new JList(listModel);
        list.setSelectedIndex(0);
        list.setFocusable(false);
        list.setCellRenderer(new CustomCellRenderer());
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                JList list = (JList) evt.getSource();
                int index = list.locationToIndex(evt.getPoint());
                Object item = listModel.getElementAt(index);
                String filePath = ((File) item).getPath();
                try {
                    textArea.append(read(filePath));
                } catch (java.io.FileNotFoundException ex) {
                    System.out.println("File not found");
                }

            }
        });
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                String filePath = ((File) list.getSelectedValue()).getPath();
                try {
                    isCreated = false;
                    textArea.append(read(filePath));
                } catch (java.io.FileNotFoundException ex) {
                    System.out.println("File not found");
                }
            }
        });

        JButton openButton = new JButton("Открыть текст");

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg) {
                tagPairs = new LinkedHashMap<Pair<String, String>, Integer>();
                tagWordAmount = new LinkedHashMap<Pair<String, String>, Integer>();
                tagsForEachWord = new LinkedHashMap<String, ArrayList<String>>();
                dictionary = new LinkedHashMap<String, Integer>();
                tagsAmount = new LinkedHashMap<>();
                loadDictionary();
                loadStats();
                wordCount = 0;
                JFileChooser fileChooser = new JFileChooser("texts");
                FileNameExtensionFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text");
                fileChooser.setFileFilter(filter);
                int ret = fileChooser.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    isFileOpened = true;
                    File file = fileChooser.getSelectedFile();
                    currentFile = file;
                    taggedFile = new File("taggedText.txt");
                    listModel.addElement(file);
                    int index = listModel.size() - 1;
                    list.setSelectedIndex(index);
                    list.ensureIndexIsVisible(index);
                    isCreated = false;

                    String filePath = fileChooser.getSelectedFile().getPath();
                    try {
                        text = read(filePath);
                        textArea.append(text);

                        allTags = new LinkedHashMap<>();
                        BufferedReader reader = new BufferedReader(new FileReader(new File("tags.txt")));
                        String s = reader.readLine();
                        while (s != "" && s != null){
                            allTags.put(s.substring(s.indexOf(".\t") + 2, s.lastIndexOf("\t")),
                                    s.substring(s.lastIndexOf("\t")));
                            s = reader.readLine();
                        }

                    } catch (java.io.FileNotFoundException e) {
                        textArea.append("File not found");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        panel.setLayout(new BorderLayout());
        panel.add(openButton, BorderLayout.NORTH);
        panel.add(createDictionaryButton, BorderLayout.SOUTH);
        panel.add(subPanel, BorderLayout.EAST);
        panel.add(list, BorderLayout.WEST);
        panel.add(scroll, BorderLayout.CENTER);

        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}