package net.aclrian.mpe.pfarrei;

import net.aclrian.mpe.messe.*;
import net.aclrian.mpe.utils.*;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.parsers.*;
import java.io.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.regex.*;

public class ReadFilePfarrei {

    private ReadFilePfarrei() {
    }

    public static Pfarrei getPfarrei(String pfadMitDateiundmitEndung) throws ParserConfigurationException, IOException, SAXException {
        Pfarrei pf = null;
        File fXmlFile = new File(pfadMitDateiundmitEndung);
        String s = fXmlFile.getAbsolutePath();
        if (s.endsWith(DateienVerwalter.PFARREI_DATEIENDUNG)) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newDefaultInstance();
            dbFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            if (doc != null) {
                doc.getDocumentElement().normalize();

                String name;
                boolean hochaemter;
                ArrayList<StandardMesse> sm = new ArrayList<>();
                Einstellungen einst = new Einstellungen();

                // Standardmessen
                NodeList nL = doc.getElementsByTagName("std_messe");
                for (int j = 0; j < nL.getLength(); j++) {
                    Node nsm = nL.item(j);
                    if (nsm.getNodeType() == 1) {
                        Element eElement = (Element) nsm;
                        String tag = eElement.getElementsByTagName("tag").item(0).getTextContent();

                        TemporalAccessor accessor = DateUtil.SHORT_STANDALONE.parse(tag);
                        DayOfWeek dow = DayOfWeek.from(accessor);
                        int std = Integer
                                .parseInt(eElement.getElementsByTagName("std").item(0).getTextContent());
                        String min = eElement.getElementsByTagName("min").item(0).getTextContent();
                        String ort = eElement.getElementsByTagName("ort").item(0).getTextContent();
                        int anz = Integer
                                .parseInt(eElement.getElementsByTagName("anz").item(0).getTextContent());
                        String typ = eElement.getElementsByTagName("typ").item(0).getTextContent();
                        StandardMesse stdm = new StandardMesse(dow, std, min, ort, anz, typ);
                        sm.add(stdm);
                    }
                }
                hochaemter = readEinstellungen(einst, doc);
                String[] s2 = pfadMitDateiundmitEndung.split(Pattern.quote(File.separator));
                name = s2[s2.length - 1];
                name = name.substring(0, name.length() - DateienVerwalter.PFARREI_DATEIENDUNG.length());
                name = name.replace("_", " ");
                sm.add(new Sonstiges());
                pf = new Pfarrei(einst, sm, name, hochaemter);
            }
        }
        return pf;
    }

    private static boolean readEinstellungen(Einstellungen einst, Document doc) {
        NodeList nEii = doc.getElementsByTagName("Einstellungen");
        Node n = nEii.item(0);
        NodeList nEi = n.getChildNodes();
        boolean hochaemter = false;
        for (int j = 0; j < nEi.getLength(); j++) {
            Node ne = nEi.item(j);
            if (ne.getNodeType() == 1) {
                Element eE = (Element) ne;
                if (eE.getTagName().equals("hochaemter")) {
                    String booleany = eE.getTextContent();
                    if (booleany.equals("1")) {
                        hochaemter = true;
                    }
                    continue;
                }
                readSetting(einst, eE);
            }
        }
        return hochaemter;
    }

    private static void readSetting(Einstellungen einst, Element eE) {
        String val = eE.getTextContent();
        int anz = Integer.parseInt(val);
        if (eE.hasAttribute("year")) {
            String id = eE.getAttribute("year");
            int i = Integer.parseInt(id);
            if (i < Einstellungen.LENGTH && i > -1) {
                einst.editiereYear(i, anz);
            } else
                Log.getLogger().info("id ist zu groß!");
        } else if (eE.hasAttribute("Lleiter")) {
            String id = eE.getAttribute("Lleiter");
            int i = Integer.parseInt(id);
            if (i == 0 || i == 1) {
                einst.editMaxDienen(id.equals("1"), anz);
            } else {
                Log.getLogger().info("id Fehler!");
            }

        } else {
            Log.getLogger().warn("unbekannte Node: {}", eE.getTagName());
        }
    }
}
