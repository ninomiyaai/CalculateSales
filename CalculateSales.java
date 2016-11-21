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

		if (args.length != 1) {
			System.out.println("予期せぬエラーが発生しました");
			return;
		}

		if((input(args[0], "branch.lst",  "^[0-9]{3}$", branchFile, branchMoney)) == false) {
			return;
		}
		if((input(args[0], "commodity.lst", "^[0-9a-zA-Z]{8}$", commodityFile, commodityMoney)) == false) {
			return;
		}

		// 売上ファイルのみのリスト
		ArrayList<String> rcdList = new ArrayList<String>();
		File calDir = new File(args[0]);
		File[] rcdFile = calDir.listFiles();

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
				// commodityFileに商品コードがあるかどうか
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

		if((output(branchMoney, args[0], "branch.out", branchFile)) == false) {
			return;
		}
		if((output(commodityMoney, args[0], "commodity.out", commodityFile)) == false) {
			return;
		}
	}


	public static boolean input(String CalculateSalesDir, String lstFile,
			String lstFileFormat, HashMap<String, String> fileMap, HashMap<String, Long> moneyMap) {

		BufferedReader br = null;
		try {
			File file = new File(CalculateSalesDir, lstFile);
			if (!file.exists()) {
				System.out.println("支店定義ファイルが存在しません");
				return false;
			}
			br = new BufferedReader(new FileReader(file));
			String listFile1Line;


			while ((listFile1Line = br.readLine()) != null) {
				// ファイル内の一行をカンマで切って要素分けする
				String[] data = listFile1Line.split(",");
				// 支店名にカンマ、改行があるかチェック
				// 要素数が2と決まってるので(要素数 != 2)
				if (data.length != 2) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return false;
				}
				// 支店コードが3桁の数字のみであるかチェック
				// ^...先頭 {}...直前の数字の回数 $...末尾
				if (!(data[0].matches(lstFileFormat))) {
					System.out.println("支店定義ファイルのフォーマットが不正です");
					return false;
				}
				fileMap.put(data[0], data[1]);
				moneyMap.put(data[0], (long)0);
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			try {
				if(br != null){
					br.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}

	public static boolean output(HashMap<String, Long> moneyMap, String CalculateSalesDir,
			String outFile, HashMap<String, String> FileMap) {

		List<Map.Entry<String, Long>>sortMoneyMap  = new ArrayList<Map.Entry<String, Long>>(moneyMap.entrySet());
		Collections.sort(sortMoneyMap, new Comparator<Map.Entry<String, Long>>() {
			public int compare(Entry<String, Long> entry1, Entry<String, Long> entry2) {
				return ((Long) entry2.getValue()).compareTo((Long) entry1.getValue());
			}
		});

		BufferedWriter bw = null;
		try {
			File file = new File(CalculateSalesDir, outFile);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileWriter fw = new FileWriter(file);
			bw = new BufferedWriter(fw);

			for (Entry<String, Long> s : sortMoneyMap) {
				bw.write(s.getKey() + "," + FileMap.get(s.getKey()) + "," + s.getValue()
						+ System.getProperty("line.separator"));
			}
		} catch (IOException e) {
			System.out.println("予期せぬエラーが発生しました");
			return false;
		} finally {
			try {
				if(bw != null){
					bw.close();
				}
			} catch (IOException e) {
				System.out.println("予期せぬエラーが発生しました");
				return false;
			}
		}
		return true;
	}
}
