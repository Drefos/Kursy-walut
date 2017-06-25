package app;


import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DataReader {

    private final String SEARCH_URL = "http://www.nbp.pl/home.aspx?f=/kursy/kursya.html";

    private ObservableList<Currency> currencies;
    public static String date;
    public static final String FILE_STORE = "currency.info";

    private List<String> toSave;


    public ObservableList<Currency> get() {
        return currencies;
    }

    public DataReader() {
        currencies = FXCollections.observableArrayList();
        toSave = new ArrayList<>();
    }

    public void search() throws IOException {
        Document d = Jsoup.connect(SEARCH_URL).get();
        Elements u = d.getElementsByAttributeValue("class", "file print_hidden");
        Element ur = u.select("a[href]").first();
        String url = ur.attr("href");
        Document doc = Jsoup.connect("http://www.nbp.pl" + url).get();
        date = doc.select("data_publikacji").text();
        Elements bgt = doc.getElementsByTag("pozycja");
        List<Currency> list = new ArrayList<>();
        List<String> temp = new ArrayList<>();
        temp.add(date);
        for (Element e : bgt) {
            String country = e.select("nazwa_waluty").text();
            String code = e.select("kod_waluty").text();
            Double value1 = new Double(e.select("kurs_sredni").text().replace(',', '.'));
            Double value2 = new Double(e.select("przelicznik").text().replace(',', '.'));
            String value = multiple(value1, value2).toString();
            temp.add(country);
            temp.add(code);
            temp.add(value);
            list.add(new Currency(country, code, value));
        }
        currencies.clear();
        currencies.addAll(list);
        toSave.clear();
        toSave.addAll(temp);
    }


    public void write() {
        File file = new File(FILE_STORE);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (FileOutputStream fos = new FileOutputStream(FILE_STORE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            oos.writeObject(toSave);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }


    public void read() {
        File file = new File(FILE_STORE);
        if (!file.exists()) {
            return;
        }
        try (FileInputStream fis = new FileInputStream(FILE_STORE);
             ObjectInputStream ois = new ObjectInputStream(fis)) {

            List<String> readList = (ArrayList<String>) ois.readObject();
            toSave = readList;
            if (readList.size() > 0) {
                List<Currency> list = new ArrayList<>();
                int i = 0;
                date = readList.get(i++);
                while (i < readList.size()) {
                    String country = readList.get(i++);
                    String code = readList.get(i++);
                    String value = readList.get(i++);
                    list.add(new Currency(country, code, value));
                }
                currencies.clear();
                currencies.addAll(list);
            }
        } catch (IOException e) {
            e.getStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }


    public Double countResult(double pln, double currency) {
        return new Double(Math.round(pln*currency*10000))/10000;
    }

    private Double multiple(double v1, double v2) {
        return new Double(Math.round(v1*v2*10000))/10000;
    }

}

