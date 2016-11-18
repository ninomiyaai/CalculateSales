package jp.co.abilitynet.ninomiya_ai.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CalculateSales {
	public static void main(String[] args) {

		// 支店コード・支店名
		HashMap<String, String> branchFile = new HashMap<String, String>();
		// 支店コード・支店金額
		HashMap<String, Long> branchMoney = new HashMap<String, Long>();
		// 商品コード・商品名
		HashMap<String, String> commodityFile = new HashMap<String, String>();
		// 商品コード・商品金額
		HashMap<String, Long> commodityMoney = new HashMap<String, Long>();

		BufferedReader br = null;



		try {
			if (args.length != 1) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}

			File file = new File(args[0], "branch.lst");
			if (!file.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String branch;

			while ((branch = br.readLine()) != null) {
				// ファイル内の一行をカンマで切って要素分けする
				String[] branchData = branch.split(",");
				// 支店名にカンマ、改行があるかチェック
				// 要素数が2と決まってるので(要素数 != 2)
				if (branchData.length != 2) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				// 支店コードが3桁の数字のみであるかチェック
				// ^...先頭 {}...直前の数字の回数 $...末尾
				if (!(branchData[0].matches("^[0-9]{3}$"))) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return;
				}
				branchFile.put(branchData[0], branchData[1]);
				branchMoney.put(branchData[0], (long)0);
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		try {
			File file = new File(args[0], "commodity.lst");
			if (!file.exists()) {
				System.out.println("商品定義ファイルが存在しません");
				return;
			}
			br = new BufferedReader(new FileReader(file));
			String commodity;
			while ((commodity = br.readLine()) != null) {
				String[] commodityData = commodity.split(",");

				if (commodityData.length != 2) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				if (!(commodityData[0].matches("^[0-9a-zA-Z]{8}$"))) {
					System.out.println("商品定義ファイルのフォーマットが不正です");
					return;
				}
				commodityFile.put(commodityData[0], commodityData[1]);
				commodityMoney.put(commodityData[0], (long)0);
			}

		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		// 売上ファイルのみのリスト
		ArrayList<String> rcdList = new ArrayList<String>();
		File CalDir = new File(args[0]);
		File[] rcdFile = CalDir.listFiles();

		// ディレクトリからrcdファイルのみ列挙
		for (int i = 0; i < rcdFile.length; i++) {
			// 8桁数字 と .rcd をチェック
			// 且つ、ディレクトリ内のファイルのみにする(.rcdのフォルダがあったらそれも拾ってしまうので)
			if (rcdFile[i].isFile() && rcdFile[i].getName().matches("^[0-9]{8}\\.rcd$")) {
				// rcdファイルのみ入っているリストを作る
				rcdList.add(rcdFile[i].getName());
			}
		}
		Collections.sort(rcdList);
		// 最大値を取得
		String[] rcdMax = rcdList.get(rcdList.size() - 1).split("\\.");
		int rcdMaxIn = Integer.parseInt(rcdMax[0]);
		// 最小値を取得
		String[] rcdMin = rcdList.get(0).split("\\.");
		int rcdMinIn = Integer.parseInt(rcdMin[0]);
		// 要素数を取得
		int rcdElementCountIn = rcdList.size();
		if (!(rcdMaxIn - rcdMinIn + 1 == rcdElementCountIn)) {
			System.out.println("売上ファイル名が連番になっていません");
			return;
		}

		// 売上ファイルのみのリストの中身
		for (int i = 0; i < (rcdList.size()); i++) {
			ArrayList<String> rcd3Lines = new ArrayList<String>();
			File file = new File(args[0], rcdList.get(i));
			try {
				br = new BufferedReader(new FileReader(file));
				String sales;
				// 各rcdファイルをエラー確認して抽出
				// 3行であるかどうか ＝ 要素が3つであるかどうか
				// branchFileに支店コードがあるかどうか....map.containsKey(object k)
				// commodityFileで商品コードがあるかどうか
				while ((sales = br.readLine()) != null) {
					rcd3Lines.add(sales);
				}
				if (rcd3Lines.size() != 3) {
					System.out.println(rcdFile[i].getName() + "のフォーマットが不正です");
					return;
				}
				if (!(branchFile.containsKey(rcd3Lines.get(0)))) {
					System.out.println(rcdFile[i].getName() + "の支店コードが不正です");
					return;
				}
				if (!(commodityFile.containsKey(rcd3Lines.get(1)))) {
					System.out.println(rcdFile[i].getName() + "の商品コードが不正です");
					return;
				}

				// 売上ファイルの売上額を支店と商品の合計金額に加算する
				// 支店金額マップ.put((支店コード),(支店金額 + 売上額);
				// 支店コード rcd3Lines.get(0) = branchData[0]
				// 支店金額は支店金額マップの value なので(金額マップ.get(object k))
				long branchSalesLong = Long.parseLong(rcd3Lines.get(2));
				branchMoney.put((rcd3Lines.get(0)), (branchMoney.get(rcd3Lines.get(0))) + branchSalesLong);
				String branchSales = String.valueOf(branchSalesLong);
				// matches は String型のみ
				if (branchSales.matches("^[0-9]{10,}$")) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}

				long commoditySalesLong = Long.parseLong(rcd3Lines.get(2));
				commodityMoney.put((rcd3Lines.get(1)), (commodityMoney.get(rcd3Lines.get(1))) + commoditySalesLong);
				String commoditySales = String.valueOf(commoditySalesLong);
				if (branchSales.matches("^[0-9]{10,}$")) {
					System.out.println("合計金額が10桁を超えました");
					return;
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			} catch (NumberFormatException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}	finally {
				try {
					if(br != null){
						br.close();
					}
				} catch (IOException e) {
					System.out.println("予期せぬエラーが発生しました");
					return;
				}
			}
		}

		// sortする
		List<Map.Entry<String, Long>> branchAll = new ArrayList<Map.Entry<String, Long>>(branchMoney.entrySet());
		Collections.sort(branchAll, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}
		});

		BufferedWriter bw = null;
		try {
			File file = new File(args[0], "branch.out");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (Entry<String, Long> s : branchAll) {
				bw.write(s.getKey() + "," + branchFile.get(s.getKey()) + "," + s.getValue()
						+ System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if(bw != null){
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

		List<Map.Entry<String, Long>> commodityAll = new ArrayList<Map.Entry<String, Long>>(
				commodityMoney.entrySet());
		Collections.sort(commodityAll, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}
		});

		try {
			File file = new File(args[0], "commodity.out");
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);
			for (Entry<String, Long> s : commodityAll) {
				bw.write(s.getKey() + "," + commodityFile.get(s.getKey()) + "," + s.getValue()
						+ System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		} finally {
			try {
				if(bw != null){
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return;
			}
		}

	}
}
