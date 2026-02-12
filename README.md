# MyRecipes

## 概要
「基礎＆応用力をしっかり育成！Androidアプリ開発の教科書 第3版 Java対応」の応用・発展として個人で制作した写真付きのレシピ管理アプリです。
レシピの登録・編集・削除・検索に加え、写真の保存・表示機能も実装しました。
CRUD処理を中心に、データベースを活用したアプリ開発を目的として制作しました。

## 開発形態
個人開発

## 主な機能
- レシピ一覧表示（MainActivity）
- レシピの新規登録（AddActivity）
- レシピの編集（EditActivity）
- レシピの削除
- レシピ名やカテゴリーなどによる検索機能
- 写真の登録・表示機能（ギャラリーから選択orカメラ起動）
- 入力チェック（未入力防止）

## 使用技術
- Android Studio
- Java
- SQLite

## 工夫した点

- **Activityごとの役割分担（設計の工夫）**  
  AddActivity・EditActivityなど、機能ごとに画面を分けることでコードの可読性と保守性を高めました。
- **SQLiteを用いたデータ管理**  
  CRUD（作成・参照・更新・削除）処理を実装。データベース操作の基礎を理解し、検索機能も組み込みました。
- **柔軟な検索機能の実装**  
  レシピ名の部分一致検索に加え、カテゴリーや調理時間、お気に入り状態での絞り込みを可能にし、ユーザーが目的のレシピを素早く見つけられるようにしました。
- **写真登録機能の追加**  
  カメラ撮影やギャラリーからの画像取得に対応。
- **ユーザビリティへのこだわり**  
  未入力チェックのバリデーションや、Toastによる親切なエラー通知を実装。また、編集画面では既存データが自動入力されるなど、ストレスのない操作感を意識しました。



## スクリーンショット
<img width="30%" height="2400" alt="Screenshot_1770859031" src="https://github.com/user-attachments/assets/2df42740-ab12-44bc-a538-75d8764154ad" />　<img width="30%" height="2400" alt="Screenshot_1770862011" src="https://github.com/user-attachments/assets/a05d3da3-403b-477c-88a7-603480afbf1c" />　<img width="30%" height="2400" alt="Screenshot_1770857177" src="https://github.com/user-attachments/assets/f56f0f39-129d-4b63-867b-9449d8e8c7c7" />

<img width="30%" height="2400" alt="Screenshot_1770859053" src="https://github.com/user-attachments/assets/1d70c188-6546-43f3-ac7d-425df52c3a33" />　<img width="30%" height="2400" alt="Screenshot_1770859312" src="https://github.com/user-attachments/assets/ef5ccded-1bd7-4ed7-b5b2-9f002577f2e4" />


## GIF



