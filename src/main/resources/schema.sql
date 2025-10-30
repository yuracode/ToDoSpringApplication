-- テーブルが存在したら削除する
DROP TABLE IF EXISTS todos;
DROP TABLE IF EXISTS authentications;
DROP TYPE IF EXISTS role;

-- テーブルの作成
CREATE TABLE todos (
	-- id（することID）：主キー
    id serial PRIMARY KEY,
    -- todo（すること）：NULL不許可
    todo varchar(255) NOT NULL,
    -- detail（説明）
    detail text,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    priority INT NOT NULL DEFAULT 2,
    
    category varchar(100) NOT NULL DEFAULT 'プライベート',
    -- created_at（作成日）
    created_at timestamp without time zone,
    -- updated_at（更新日）
    updated_at timestamp without time zone
);

