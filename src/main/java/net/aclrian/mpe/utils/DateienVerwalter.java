package net.aclrian.mpe.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import net.aclrian.mpe.messdiener.Messdiener;
import net.aclrian.mpe.messdiener.ReadFile;
import net.aclrian.mpe.pfarrei.Pfarrei;
import net.aclrian.mpe.pfarrei.ReadFile_Pfarrei;

/**
 * Sonstige Klasse, die viel mit Ordnerverwaltung und Sortieren zu tun hat.
 * 
 * @author Aclrian
 *
 */
public class DateienVerwalter {
	/**
	 * Hier wird der Pfad gespeichert, indem die Messdiener gespeichert werden
	 * sollen / sind <br>
	 * andere Klassen erzeugen hiermit neue Messdiener an dem selben Ort</br>
	 */
	private String savepath;
	private Pfarrei pf;
	private ArrayList<Messdiener> medis;
	private Window window;

	public static final String pfarredateiendung = ".xml.pfarrei";
	public static final String textdatei = File.separator+".messdienerOrdnerPfad.txt";

	
	public static DateienVerwalter dv;
	
	public static void re_start(Window window) throws NoSuchPfarrei {
		dv = new DateienVerwalter(window);
	}
	
	private DateienVerwalter(Window window) throws NoSuchPfarrei {
		this.window = window;
		this.getSpeicherort();
		File f = getPfarreFile();
		if(f == null) {
			throw new NoSuchPfarrei(savepath);
		}
		pf = ReadFile_Pfarrei.getPfarrei(f.getAbsolutePath());
	}

	
	public Pfarrei getPfarrei() {
		if(pf==null) {
			reloadPfarrei();
		}
		return pf;
	}
	
	private void reloadPfarrei() {
		File f = getPfarreFile();
		if (f == null) {
			Dialogs.fatal("Es konnte keine Pfarrei gefunden werden.");
			return;
		}
		Log.getLogger().info("Pfarrei gefunden in: " + f);
		pf = ReadFile_Pfarrei.getPfarrei(f.getAbsolutePath());
	}

	private ArrayList<File> getPfarreiFiles(){
		ArrayList<File> files = new ArrayList<>();
		File f = new File(savepath);
		for (File file : Objects.requireNonNull(f.listFiles())) {
			String s = file.toString();
			if (s.endsWith(pfarredateiendung)) {
				files.add(file);
			}

		}
		return files;
	}

	private File getPfarreFile() {
		ArrayList<File> files = getPfarreiFiles();
		if (files.size() != 1) {
			if (files.size() > 1) {
				Dialogs.warn("Es darf nur eine Datei mit der Endung: '" + pfarredateiendung+ "' in dem Ordner: " + savepath + " vorhanden sein.");
				return files.get(0);
			} else {
				return null;
			}
		} else {
			return files.get(0);
		}
	}


	public void removeoldPfarrei(File neuePfarrei) {
		ArrayList<File> files = getPfarreiFiles();
		ArrayList<File> todel = new ArrayList<>();
		boolean candel = false;
		for (File f: files) {
				if (!f.getAbsolutePath().contentEquals(neuePfarrei.getAbsolutePath())){
					todel.add(f);
				}else candel=true;
		}
		if (candel) todel.forEach(File::delete);
	}

	/**
	 * 
	 * @return Ausgewaehlten Ordnerpfad
	 */
	private String waehleOrdner() {
		DirectoryChooser f = new DirectoryChooser();
		String s = "Ordner wählen, in dem alles gespeichert werden soll:";
		f.setTitle(s);
		File file = f.showDialog(window);
		return file == null ? null : file.getPath();
	}

	private ArrayList<File> getPaths(File file, ArrayList<File> list) {
		if (file == null || list == null || !file.isDirectory())
			return null;
		File[] fileArr = file.listFiles();
		assert fileArr != null;
		for (File f : fileArr) {
			if (f.isDirectory()) {
				getPaths(f, list);
			}
			list.add(f);
		}
		return list;
	}

	private ArrayList<File> getAlleMessdienerFiles(String path) {// 2
		Log.getLogger().info("verzName: " + path);
		ArrayList<File> files = getPaths(new File(path), new ArrayList<>());
		if (files == null) {
			return new ArrayList<>();
		}
		return files;
	}

	/**
	 * 
	 * @return Messdiener als List
	 */
	public ArrayList<Messdiener> getAlleMedisVomOrdnerAlsList() {
		if (pf == null) {
			reloadPfarrei();
		}
		if (medis == null) {
			ArrayList<File> files = getAlleMessdienerFiles(savepath);
			medis = new ArrayList<>();
			for (File file : files) {
				ReadFile rf = new ReadFile();
				Messdiener m = rf.getMessdiener(file.getAbsolutePath());
				if (m != null) {
					medis.add(m);
				}
			}
			for (Messdiener medi : medis) {
				medi.setnewMessdatenDaten();
			}
		}
		return medis;
	}

	public String getSavepath() {
		if (savepath == null || savepath.equals("")) {
			savepath = waehleOrdner();
		}
		return savepath;
	}

	private void setSavepath(String savepath) {
		this.savepath = savepath;
	}

	public void erneuereSavepath() {
		String homedir = System.getProperty("user.home");
		homedir = homedir + textdatei;
		File f = new File(homedir);
		if(f.delete()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			savepath="";
			getSpeicherort();
		} else Dialogs.warn("Konnte die Datei " + homedir + " nicht ändern.");
	}

	private void getSpeicherort() {
		String homedir = System.getProperty("user.home");
		homedir = homedir + textdatei;
		Log.getLogger().info("Das Home-Verzeichniss wurde gefunden: " + homedir);
		File f = new File(homedir);
		if (!f.exists()) {
			String s;
			if (savepath == null || savepath.equals("")) {
				s = this.waehleOrdner();
			} else {
				s = savepath;
			}
			try {
				f.createNewFile();
				FileWriter fileWriter = new FileWriter(homedir);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				if (s != null) {
					Log.getLogger().info(s);
					bufferedWriter.write(s);
					setSavepath(s);
					bufferedWriter.close();
				} else {
					savepath="";
					Dialogs.warn("Es wird ein Speicherort benötigt, um dort Messdiener zu speichern.\nBitte einen Speicherort eingeben!");
					getSpeicherort();
				}
			} catch (IOException e) {
				Dialogs.error(e, "Der Speicherort konnte nicht gespeichert werden.");
			}
		} else {
			try {
				InputStreamReader fileReader = new InputStreamReader(new FileInputStream(homedir), StandardCharsets.UTF_8);
				BufferedReader bufferedReader = new BufferedReader(fileReader);
				String line = bufferedReader.readLine();
				File savepath = new File(line);
				if (savepath.exists()) {
						setSavepath(line);
				} else {
						Log.getLogger().info("Der Speicherort aus '" + f + "' ('"+line+"') existiert nicht!");
						f.delete();
						getSpeicherort();
				}
				if(this.savepath==null || this.savepath.equals("")){
					f.delete();
					getSavepath();
				}
				bufferedReader.close();
			} catch (IOException e) {
				Dialogs.error(e, "Die Datei '" + homedir+ "' konnte nicht gelesen werden.");
			}
			if(savepath == null) {
				savepath = waehleOrdner();
				try {
					f.createNewFile();
					FileWriter fileWriter = new FileWriter(homedir);
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
					if (savepath != null) {
						bufferedWriter.write(savepath);
						bufferedWriter.close();
					} else {
						Dialogs.warn("Es wird ein Speicherort benötigt, um dort Messdiener zu speichern.\nBitte einen Speicherort eingeben!");
						getSpeicherort();
					}
				} catch (IOException e) {
					Log.getLogger().info("Auf den Speicherort '" + f + "' kann nicht zugegriffen werden!");
					getSpeicherort();
				}
			}
		}
		Log.getLogger().info("Der Speicherort liegt in: " + savepath);
	}

	public void reloadMessdiener() {
		medis = null;
	}

	public static class NoSuchPfarrei extends Exception{
		private final String savepath;
		public NoSuchPfarrei(String savepath) {
			this.savepath=savepath;
		}
		public String getSavepath() {
			return savepath;
		}
	}
}
