# ToDoアプリ改造課題

## 概要
基本的なToDoアプリ（CRUD機能のみ）を段階的に機能拡張していきます。
各レベルの課題を順番に実装することで、Spring Boot + MyBatisの実践力を身につけます。

---

## 初級課題（基本機能の追加）

### 課題1：優先度機能の追加

**目的**  
プルダウンでの選択、データベースへの保存、ソート機能を学ぶ

**実装内容**

#### 1. データベース修正
```sql
-- schema.sqlを修正
DROP TABLE IF EXISTS todos;

CREATE TABLE todos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL DEFAULT 2
);
```

**priorityの値（数値で管理）**
- `1` = 高
- `2` = 中
- `3` = 低

**ヒント**
- `schema.sql`を修正後、アプリを再起動するとテーブルが再作成されます
- 既存データは消えるので注意（開発中は問題なし）

#### 2. Entityクラスの修正
```java
// ToDo.java
public class ToDo {
    private Integer id;
    private String title;
    private Boolean completed;
    private Integer priority;  // ← 追加（数値型）
    
    // getter/setterも追加
}
```

#### 3. フォームの修正
```html
<!-- form.html -->
<form th:action="@{/todo/register}" method="post">
    <label>タイトル：</label>
    <input type="text" name="title" required>
    
    <label>優先度：</label>
    <select name="priority" required>
        <option value="1">高</option>
        <option value="2" selected>中</option>
        <option value="3">低</option>
    </select>
    
    <button type="submit">登録</button>
</form>
```

#### 4. 一覧画面の修正
```html
<!-- list.html -->
<table>
    <tr>
        <th>ID</th>
        <th>タイトル</th>
        <th>優先度</th>
        <th>完了</th>
    </tr>
    <tr th:each="todo : ${toDoList}">
        <td th:text="${todo.id}"></td>
        <td th:text="${todo.title}"></td>
        <td>
            <span th:if="${todo.priority == 1}">高</span>
            <span th:if="${todo.priority == 2}">中</span>
            <span th:if="${todo.priority == 3}">低</span>
        </td>
        <td th:text="${todo.completed} ? '完了' : '未完了'"></td>
    </tr>
</table>
```

#### 5. MapperとXMLの修正
```xml
<!-- ToDoMapper.xml -->
<insert id="insert">
    INSERT INTO todos (title, completed, priority)
    VALUES (#{title}, #{completed}, #{priority})
</insert>

<select id="selectAll" resultType="com.example.webapp.entity.ToDo">
    SELECT id, title, completed, priority
    FROM todos
    ORDER BY priority, id DESC
</select>
```

**実装のヒント**
- まずはデータベースのカラム追加から始める
- 登録画面で選択できるようにする（value="1", "2", "3"）
- 一覧画面で数値を文字列（高・中・低）に変換して表示
- ORDER BYは数値なのでシンプル（1→2→3の順）
- 数値が小さいほど優先度が高い設計

**確認ポイント**
- [ ] 新規登録時に優先度を選択できる
- [ ] 一覧画面で優先度が表示される
- [ ] 優先度順（高→中→低）で表示される

---

### 課題2：カテゴリ機能の追加

**目的**  
セレクトボックスでのカテゴリ選択、フィルター機能を学ぶ

**実装内容**

#### 1. データベース修正
```sql
-- schema.sqlを修正
DROP TABLE IF EXISTS todos;

CREATE TABLE todos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL DEFAULT 2,
    category VARCHAR(20) NOT NULL DEFAULT 'プライベート'
);
```

**categoryの値**
- `仕事`
- `プライベート`
- `勉強`

**ヒント**
- priorityカラムがすでに追加されている前提です
- `schema.sql`を修正後、アプリを再起動

#### 2. Entityクラスの修正
```java
// ToDo.java
public class ToDo {
    private Integer id;
    private String title;
    private Boolean completed;
    private Integer priority;
    private String category;  // ← 追加
    
    // getter/setterも追加
}
```

#### 3. フォームの修正
```html
<!-- form.html -->
<label>カテゴリ：</label>
<select name="category" required>
    <option value="仕事">仕事</option>
    <option value="プライベート" selected>プライベート</option>
    <option value="勉強">勉強</option>
</select>
```

#### 4. 一覧画面にフィルター追加
```html
<!-- list.html -->
	<form th:action="@{/todos/list}" method="get">
	    <label for="category">カテゴリで絞り込み：</label>
	    <select id="category" name="category">
	        <option value=""
	            th:selected="${category == null or category == ''}">全て</option>
	        <option value="仕事"
	            th:selected="${category == '仕事'}">仕事</option>
	        <option value="プライベート"
	            th:selected="${category == 'プライベート'}">プライベート</option>
	        <option value="勉強"
	            th:selected="${category == '勉強'}">勉強</option>
	    </select>
	    <button type="submit">絞り込み</button>
	</form>
```

#### 5. Controllerの修正
```java
     /**
     * 「すること」の一覧をカテゴリーで絞り込んで表示します。
     */
	@GetMapping("/list")
	public String list(@RequestParam(required = false) String category,Model model) {
		if(category.isEmpty()) {
			return "redirect:/todos";
		}
		model.addAttribute("category", category);
		model.addAttribute("todos", toDoService.findByCategory(category));
		return "todo/list";
	}
```

#### 6. Service、Mapper、XMLの追加
```java
// ToDoService.java
    /**
	 * 指定されたカテゴリーの「すること」を検索します。
	 */
    public List<ToDo> findByCategory(String category) {
		// Mapperにカテゴリーを渡して検索を実行
		return toDoMapper.selectByCategory(category);
	}

// ToDoMapper.java
    /** 
     * 指定されたカテゴリーに対応する「すること」を取得します。
     */
    List<ToDo> selectByCategory(@Param("category") String category);
```

```xml
<!-- ToDoMapper.xml -->
	<!-- カテゴリー検索 -->
	<select id="selectByCategory" resultType="com.example.todo.entity.ToDo">
    SELECT id, todo, detail, completed, priority, category,
		created_at ,updated_at FROM todos WHERE category = #{category}
	</select>
```

**実装のヒント**
- 優先度と同じ流れで実装
- `@RequestParam`でGETパラメータを受け取る
- `required = false`で省略可能にする
- 空文字チェックを忘れずに

**確認ポイント**
- [ ] 新規登録時にカテゴリを選択できる
- [ ] 一覧画面でカテゴリが表示される
- [ ] カテゴリで絞り込みができる
- [ ] 「全て」を選ぶと全件表示される

---

## 中級課題（機能拡張）

### 課題3：タイトル検索機能

**目的**  
LIKE句を使った部分一致検索、入力フォームからのパラメータ受け取りを学ぶ

**実装内容**

#### 1. 一覧画面に検索フォーム追加
```html
<!-- list.html -->
<form th:action="@{/todo/list}" method="get">
    <label>タイトルで検索：</label>
    <input type="text" name="keyword" th:value="${keyword}" placeholder="キーワードを入力">
    <button type="submit">検索</button>
</form>
```

#### 2. Controllerの修正
```java
@GetMapping("/list")
public String list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        Model model) {

    // 両方空ならリダイレクト
    if ((keyword == null || keyword.isEmpty()) &&
        (category == null || category.isEmpty())) {
        return "redirect:/todos";
    }

    List<ToDo> todos;

    if (keyword != null && !keyword.isEmpty()) {
        todos = toDoService.searchByTitle(keyword);
    } else {
        todos = toDoService.findByCategory(category);
    }

    model.addAttribute("todos", todos);
    model.addAttribute("keyword", keyword);
    model.addAttribute("category", category);
    return "todo/list";
}
```

#### 3. Service、Mapper、XMLの追加
```java
// ToDoService.java
public List<ToDo> searchByTitle(String keyword) {
    return toDoMapper.searchByTitle(keyword);
}

// ToDoMapper.java
List<ToDo> searchByTitle(@Param("keyword") String keyword);
```

```xml
<!-- ToDoMapper.xml -->
<select id="searchByTitle" resultType="com.example.webapp.entity.ToDo">
    SELECT id, title, completed, priority, category
    FROM todos
    WHERE title LIKE CONCAT('%', #{keyword}, '%')
    ORDER BY id DESC
</select>
```

**実装のヒント**
- `LIKE '%keyword%'`で部分一致検索
- H2では`CONCAT`関数を使う
- `th:value="${keyword}"`で検索後も入力値を保持
- 空文字の場合は全件表示

**確認ポイント**
- [ ] キーワード入力で検索できる
- [ ] 部分一致で検索される（「買」で「買い物」がヒット）
- [ ] 検索後も入力したキーワードが残る
- [ ] 空欄で検索すると全件表示

---

### 課題4：完了/未完了フィルター

**目的**  
ラジオボタンでの状態選択、boolean型での絞り込みを学ぶ

**実装内容**

#### 1. 一覧画面にフィルター追加
```html
<!-- list.html -->
<form th:action="@{/todo/list}" method="get">
    <label>表示：</label>
    <input type="radio" name="status" value="" checked> 全て
    <input type="radio" name="status" value="false" 
           th:checked="${status == 'false'}"> 未完了のみ
    <input type="radio" name="status" value="true"
           th:checked="${status == 'true'}"> 完了のみ
    <button type="submit">表示</button>
</form>
```

#### 2. Controllerの修正
```java
@GetMapping("/list")
public String list(
    @RequestParam(required = false) String status,
    Model model) {
    
    List<ToDo> toDoList;
    
    if (status != null && !status.isEmpty()) {
        Boolean completed = Boolean.valueOf(status);
        toDoList = toDoService.findByStatus(completed);
    } else {
        toDoList = toDoService.findAllToDo();
    }
    
    model.addAttribute("toDoList", toDoList);
    model.addAttribute("status", status);
    return "todo/list";
}
```

#### 3. Service、Mapper、XMLの追加
```java
// ToDoService.java
public List<ToDo> findByStatus(Boolean completed) {
    return toDoMapper.selectByStatus(completed);
}

// ToDoMapper.java
List<ToDo> selectByStatus(@Param("completed") Boolean completed);
```

```xml
<!-- ToDoMapper.xml -->
<select id="selectByStatus" resultType="com.example.webapp.entity.ToDo">
    SELECT id, title, completed, priority, category
    FROM todos
    WHERE completed = #{completed}
    ORDER BY id DESC
</select>
```

**実装のヒント**
- `String`で受け取って`Boolean`に変換
- `th:checked`で選択状態を保持
- `value=""`で「全て」を表現

**確認ポイント**
- [ ] 未完了のみ表示できる
- [ ] 完了のみ表示できる
- [ ] 全て表示できる
- [ ] 選択状態が保持される

---

### 課題5：並び替え機能

**目的**  
ORDER BY句の使い分け、動的SQLを学ぶ

**実装内容**

#### 1. 一覧画面に並び替え選択追加
```html
<!-- list.html -->
<form th:action="@{/todo/list}" method="get">
    <label>並び順：</label>
    <select name="sort">
        <option value="id" th:selected="${sort == 'id'}">登録日順</option>
        <option value="priority" th:selected="${sort == 'priority'}">優先度順</option>
    </select>
    <button type="submit">並び替え</button>
</form>
```

#### 2. Controllerの修正
```java
@GetMapping("/list")
public String list(
    @RequestParam(required = false, defaultValue = "id") String sort,
    Model model) {
    
    List<ToDo> toDoList = toDoService.findAllWithSort(sort);
    
    model.addAttribute("toDoList", toDoList);
    model.addAttribute("sort", sort);
    return "todo/list";
}
```

#### 3. Service、Mapper、XMLの追加
```java
// ToDoService.java
public List<ToDo> findAllWithSort(String sort) {
    return toDoMapper.selectAllWithSort(sort);
}

// ToDoMapper.java
List<ToDo> selectAllWithSort(@Param("sort") String sort);
```

```xml
<!-- ToDoMapper.xml -->
<select id="selectAllWithSort" resultType="com.example.webapp.entity.ToDo">
    SELECT id, title, completed, priority, category
    FROM todos
    <choose>
        <when test="sort == 'priority'">
            ORDER BY priority, id DESC
        </when>
        <otherwise>
            ORDER BY id DESC
        </otherwise>
    </choose>
</select>
```

**実装のヒント**
- `defaultValue`でデフォルト値を設定
- MyBatisの`<choose>`で条件分岐
- 優先度順は数値なのでシンプル（1, 2, 3順）
- 優先度が同じ場合はid順をセカンダリソートに

**確認ポイント**
- [ ] 登録日順で表示できる
- [ ] 優先度順（高→中→低）で表示できる
- [ ] 選択状態が保持される

---

## 上級課題（複雑な機能）

### 課題6：カテゴリマスタの分離（正規化）

**目的**  
テーブル設計の正規化、外部キー、JOINを学ぶ

**実装内容**

#### 1. カテゴリマスタテーブルの作成
```sql
-- schema.sqlを修正

-- カテゴリマスタテーブル
DROP TABLE IF EXISTS todos;
DROP TABLE IF EXISTS categories;

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL UNIQUE
);

-- todosテーブル（category_idで関連付け）
CREATE TABLE todos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL DEFAULT 2,
    category_id INT NOT NULL,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

```sql
-- data.sqlを修正（初期データ投入）

-- カテゴリマスタの初期データ
INSERT INTO categories (name) VALUES ('仕事');
INSERT INTO categories (name) VALUES ('プライベート');
INSERT INTO categories (name) VALUES ('勉強');

-- ToDo初期データ（category_idで指定）
INSERT INTO todos (title, completed, priority, category_id) 
VALUES ('買い物', false, 2, 2);

INSERT INTO todos (title, completed, priority, category_id) 
VALUES ('資料作成', false, 1, 1);
```

**ヒント**
- `DROP TABLE`の順番に注意（外部キー制約があるため、todosを先に削除）
- category_idは必須（NOT NULL）
- 初期データもcategory_idで指定

#### 2. Categoryエンティティの作成
```java
// Category.java
package com.example.webapp.entity;

public class Category {
    private Integer id;
    private String name;
    
    // getter/setter
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
```

#### 3. ToDoエンティティの修正
```java
// ToDo.java
public class ToDo {
    private Integer id;
    private String title;
    private Boolean completed;
    private Integer priority;
    private Integer categoryId;     // ← 追加
    private String categoryName;    // ← JOIN結果用
    
    // getter/setterも追加
}
```

#### 4. CategoryMapperの作成
```java
// CategoryMapper.java
package com.example.webapp.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.example.webapp.entity.Category;

@Mapper
public interface CategoryMapper {
    List<Category> selectAll();
}
```

```xml
<!-- CategoryMapper.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
    "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.webapp.mapper.CategoryMapper">
    
    <select id="selectAll" resultType="com.example.webapp.entity.Category">
        SELECT id, name
        FROM categories
        ORDER BY id
    </select>
    
</mapper>
```

#### 5. ToDoMapper.xmlの修正（JOIN）
```xml
<!-- ToDoMapper.xml -->
<select id="selectAll" resultType="com.example.webapp.entity.ToDo">
    SELECT 
        t.id,
        t.title,
        t.completed,
        t.priority,
        t.category_id AS categoryId,
        c.name AS categoryName
    FROM todos t
    INNER JOIN categories c ON t.category_id = c.id
    ORDER BY t.id DESC
</select>

<insert id="insert">
    INSERT INTO todos (title, completed, priority, category_id)
    VALUES (#{title}, #{completed}, #{priority}, #{categoryId})
</insert>
```

#### 6. フォームの修正（動的プルダウン）
```html
<!-- form.html -->
<label>カテゴリ：</label>
<select name="categoryId" required>
    <option th:each="category : ${categories}" 
            th:value="${category.id}"
            th:text="${category.name}"></option>
</select>
```

#### 7. Controllerの修正
```java
@GetMapping("/form")
public String form(Model model) {
    // カテゴリ一覧を取得
    List<Category> categories = categoryService.findAllCategories();
    model.addAttribute("categories", categories);
    return "todo/form";
}
```

#### 8. CategoryServiceの作成
```java
// CategoryService.java
package com.example.webapp.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.example.webapp.entity.Category;
import com.example.webapp.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    public List<Category> findAllCategories() {
        return categoryMapper.selectAll();
    }
}
```

**実装のヒント**
- まずカテゴリマスタテーブルを作成
- 外部キー制約を設定
- JOINでカテゴリ名を取得
- プルダウンはカテゴリマスタから動的生成

**確認ポイント**
- [ ] カテゴリマスタテーブルが作成されている
- [ ] 登録時にカテゴリIDが保存される
- [ ] 一覧でカテゴリ名が表示される（JOIN）
- [ ] プルダウンがマスタから生成される

**発展課題**
- カテゴリの追加・編集・削除機能
- カテゴリごとのToDo件数表示

---

## 実装の進め方

### 推奨順序
1. 優先度機能（初級・課題1）
2. カテゴリ機能（初級・課題2）
3. タイトル検索（中級・課題3）
4. 完了/未完了フィルター（中級・課題4）
5. 並び替え機能（中級・課題5）
6. カテゴリマスタ分離（上級・課題6）

### 各課題の目安時間
- 初級：1課題あたり1〜2時間
- 中級：1課題あたり2〜3時間
- 上級：課題6は4〜6時間

### デバッグのコツ
1. **エラーが出たら**
   - エラーメッセージを最後まで読む
   - どのファイルの何行目かを確認
   - スペルミス、型の違いをチェック

2. **動かないときは**
   - ブラウザのデベロッパーツールで確認
   - SQLを直接H2コンソールで実行してみる
   - `System.out.println()`でデバッグ出力

3. **困ったら**
   - 完成している部分と比較
   - エラー日記に記録
   - 先生や仲間に相談

---

## 評価ポイント

### 機能要件（60%）
- [ ] 要求された機能が正しく動作する
- [ ] エラーが発生しない
- [ ] データが正しく保存・表示される

### コード品質（30%）
- [ ] 命名規則が統一されている
- [ ] 適切にコメントが書かれている
- [ ] 重複コードがない

### プロセス（10%）
- [ ] 計画的に進められた
- [ ] 困ったときに適切に質問できた
- [ ] エラー日記をつけた

---

## 参考資料

### Spring Boot公式ドキュメント
https://spring.io/projects/spring-boot

### MyBatis公式ドキュメント
https://mybatis.org/mybatis-3/ja/

### Thymeleaf公式ドキュメント
https://www.thymeleaf.org/

---

## よくある質問（FAQ）

**Q: カラム追加後、既存データはどうなる？**  
A: `DEFAULT`を指定すれば、既存レコードにもデフォルト値が入ります。

**Q: JOINがよく分かりません**  
A: 2つのテーブルを「共通のカラム」で結合することです。カテゴリマスタ分離の課題で実践的に学べます。

**Q: エラーが出て進めません**  
A: エラーメッセージを先生に見せてください。一緒に解決しましょう。

**Q: 時間内に終わりません**  
A: 無理せず、できるところまでで大丈夫です。完璧を目指さず、動くものを作ることを優先してください。

---

**頑張ってください！分からないことがあれば、遠慮なく質問してくださいね😊**
