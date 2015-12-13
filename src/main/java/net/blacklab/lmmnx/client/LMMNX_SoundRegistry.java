package net.blacklab.lmmnx.client;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import littleMaidMobX.LMM_EnumSound;
import littleMaidMobX.LMM_LittleMaidMobNX;
import mmmlibx.lib.FileManager;
import net.blacklab.lib.obj.Pair;
import net.blacklab.lib.obj.SinglePair;

public class LMMNX_SoundRegistry {
	
	public static final String DEFAULT_TEXTURE_REGISTRATION_KEY = "!#DEFAULT#!";

	// Sound→((テクスチャ名+色)+パス)の順．
	private Map<LMM_EnumSound, HashMap<Pair<String, Integer>, String>> registerMap;
	// 実際の参照パス
	private Map<String, List<String>> pathMap;
	
	private static LMMNX_SoundRegistry instR = new LMMNX_SoundRegistry();
	
	private LMMNX_SoundRegistry() {
		registerMap = new HashMap<LMM_EnumSound, HashMap<Pair<String,Integer>,String>>();
		pathMap = new HashMap<String, List<String>>();
	}

	public static void registerSoundName(LMM_EnumSound enumSound, String texture, Integer color, String name) {
		// サウンド・ネームの登録
		Map<Pair<String, Integer>, String> map = instR.registerMap.get(enumSound);
		if (map == null) {
			instR.registerMap.put(enumSound, new HashMap<Pair<String,Integer>, String>());
			map = instR.registerMap.get(enumSound);
		} else if (map.containsKey(new SinglePair(texture, color))) {
			return;
		}
		map.put(new SinglePair<String, Integer>(texture, color), name);
	}
	
	protected static void copySoundsAdjust() {
		List sList = new ArrayList(Arrays.asList(LMM_EnumSound.values()));
		for (int i=1; i<sList.size(); i++) {
			String string = getSoundRegisteredName((LMM_EnumSound) sList.get(i), DEFAULT_TEXTURE_REGISTRATION_KEY, -1);
			LMM_LittleMaidMobNX.Debug("CHECK %s/%s", sList.get(i), string);
			if ("^".equals(string)) {
				LMM_LittleMaidMobNX.Debug("COPY");
				instR.registerMap.put((LMM_EnumSound) sList.get(i), instR.registerMap.get(sList.get(i-1)));
			}
		}
	}
	
	public static List<String> getRegisteredNamesList() {
		List<String> retmap = new ArrayList<String>();
		for (Map<Pair<String, Integer>, String> v: instR.registerMap.values()) {
			for (String f: v.values()) {
				if (!retmap.contains(f) && !f.endsWith("^") && !f.isEmpty() && f != null) retmap.add(f);
			}
		}
		return retmap;
	}
	
	public static void registerSoundPath(String name, String path) {
		// サウンドの種類を増やす
		List<String> g = instR.pathMap.get(name);
		if (g == null) {
			instR.pathMap.put(name, new ArrayList<String>());
			g = instR.pathMap.get(name);
		}
		g.add(path);
	}
	
	public static String getSoundRegisteredName(LMM_EnumSound sound, String texture, Integer color) {
		HashMap<Pair<String, Integer>, String> tMap = instR.registerMap.get(sound);
		Pair<String, Integer> value = new SinglePair<String, Integer>(null, 0);
		if (tMap != null) {
			for (Entry<Pair<String, Integer>, String> entry: tMap.entrySet()) {
				if (entry.getValue() == null || entry.getValue().isEmpty()) {
					continue;
				}
				if (entry.getKey().getKey().equals(texture) && entry.getKey().getValue() == color &&
						value.getValue() < 3) {
					LMM_LittleMaidMobNX.Debug("FOUND 3 %s", entry.getValue());
					value.setKey(entry.getValue()).setValue(3);
				}
				if (entry.getKey().getKey().equals(texture) &&
						value.getValue() < 2) {
					LMM_LittleMaidMobNX.Debug("FOUND 2 %s", entry.getValue());
					value.setKey(entry.getValue()).setValue(2);
				}
				if (entry.getKey().getValue() == color && value.getValue() < 1) {
					LMM_LittleMaidMobNX.Debug("FOUND 1 %s", entry.getValue());
					value.setKey(entry.getValue()).setValue(1);
				}
				if (value.getValue() == 0) {
					LMM_LittleMaidMobNX.Debug("FOUND 0 %s", entry.getValue());
					value.setKey(entry.getValue());
				}
			}
		}
		return value.getKey();
	}
	
	public static boolean isSoundNameRegistered(String name) {
		return getRegisteredNamesList().contains(name);
	}
	
	public static List<String> getPathListFromRegisteredName(String name) {
		return instR.pathMap.get(name);
	}
	
	public static String getPathFromRegisteredName(String name){
		List<String> g = getPathListFromRegisteredName(name);
		if (g == null) return null;
		String ret = g.get((int)(Math.random() * g.size()));
		return ret;
	}
	
	public static InputStream getSoundStream(String name) {
		String aString = getPathFromRegisteredName(name);
		LMM_LittleMaidMobNX.Debug("GETSTREAM %s", aString);
		return FileManager.COMMON_CLASS_LOADER.getResourceAsStream(aString);
	}
	
	public static InputStream getSoundStream(LMM_EnumSound sound, String texture, Integer color) {
		return getSoundStream(getSoundRegisteredName(sound, texture, color));
	}
	
}
