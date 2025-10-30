package com.example.todo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.todo.entity.ToDo;
import com.example.todo.form.ToDoForm;
import com.example.todo.service.ToDoService;

import lombok.RequiredArgsConstructor;

/**
 * ToDo：コントローラー
 */
@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class ToDoController {

    /** DIでサービスの取得 */
    private final ToDoService toDoService;

    /**
     * 「すること」の一覧を表示します。
     */
    @GetMapping
    public String list(Model model) {
        model.addAttribute("todos", toDoService.findAllToDo());
        return "todo/list";
    }

    /**
     * 指定されたIDの「すること」の詳細を表示します。
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Integer id, Model model,
            RedirectAttributes attributes) {
        // 「すること」IDに対応する「すること」情報を取得
        ToDo toDo = toDoService.findByIdToDo(id);
        if (toDo != null) {
            // 対象データがある場合はモデルに格納
            model.addAttribute("todo", toDo);
            return "todo/detail";
        } else {
            // 対象データがない場合はフラッシュメッセージを設定
            attributes.addFlashAttribute("errorMessage", "対象データがありません");
            // リダイレクト
            return "redirect:/todos";
        }
    }
    
	// === 登録・更新処理追加 ===
    /**
     * 新規登録画面を表示します。
     */
    @GetMapping("/form")
    public String newToDo(ToDoForm toDoForm) {
        // 新規登録画面の設定
        toDoForm.setIsNew(true);
        return "todo/form";
    }

    // ▽▽▽▽▽ 14.3追加 ▽▽▽▽▽
    /**
     * 新規登録を実行します。
     */
    @PostMapping("/save")
    public String create(@Validated ToDoForm toDoForm, 
    		BindingResult bindingResult, 
            	RedirectAttributes attributes) {
		// === バリデーションチェック ===
		// 入力チェックNG：入力画面を表示する
		if (bindingResult.hasErrors()) {
			// 新規登録画面の設定
			toDoForm.setIsNew(true);
			return "todo/form";
		}
        // エンティティへの変換
        ToDo todo = new ToDo();
        todo.setId(toDoForm.getId());
        todo.setTodo(toDoForm.getTodo());
        todo.setDetail(toDoForm.getDetail());
        todo.setPriority(toDoForm.getPriority());
        todo.setCompleted(toDoForm.getCompleted());
        todo.setCategory(toDoForm.getCategory());
        // 登録実行
        toDoService.insertToDo(todo);
        // フラッシュメッセージ
        attributes.addFlashAttribute("message", "新しいToDoが作成されました");
        // PRGパターン
        return "redirect:/todos";
    }
    // △△△△△ 14.3追加 △△△△△

    /**
     * 指定されたIDの修正画面を表示します。
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable("id") Integer id, Model model,
            RedirectAttributes attributes) {
        // IDに対応する「すること」を取得
        ToDo target = toDoService.findByIdToDo(id);
        if (target != null) {
            // 対象データがある場合はFormへの変換
            ToDoForm form = new ToDoForm();
            form.setId(target.getId());
            form.setTodo(target.getTodo());
            form.setDetail(target.getDetail());
            form.setPriority(target.getPriority());
            form.setCompleted(target.getCompleted());
            form.setCategory(target.getCategory());
            // 更新画面設定
            form.setIsNew(false);
            // モデルに格納
            model.addAttribute("toDoForm", form);
            return "todo/form";            
        } else {
            // 対象データがない場合はフラッシュメッセージを設定
            attributes.addFlashAttribute("errorMessage", "対象データがありません");
            // 一覧画面へリダイレクト
            return "redirect:/todos";            
        }
    }

    // ▽▽▽▽▽ 14.3追加 ▽▽▽▽▽
    /**
     * 「すること」を更新します。
     */
    @PostMapping("/update")
    public String update(@Validated ToDoForm toDoForm, 
    		BindingResult bindingResult, 
            	RedirectAttributes attributes) {
		// === バリデーションチェック ===
		// 入力チェックNG：入力画面を表示する
		if (bindingResult.hasErrors()) {
			// 更新画面の設定
			toDoForm.setIsNew(false);
			return "todo/form";
		}
        // エンティティへの変換
        ToDo todo = new ToDo();
        todo.setId(toDoForm.getId());
        todo.setTodo(toDoForm.getTodo());
        todo.setDetail(toDoForm.getDetail());
        todo.setPriority(toDoForm.getPriority());
        todo.setCompleted(toDoForm.getCompleted());
        todo.setCategory(toDoForm.getCategory());
        // 更新処理
        toDoService.updateToDo(todo);
        // フラッシュメッセージ
        attributes.addFlashAttribute("message", "ToDoが更新されました");
        // PRGパターン
        return "redirect:/todos";
    }
    // △△△△△ 14.3追加 △△△△△
   
    /**
     * 指定されたIDの「すること」を削除します。
     */
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes attributes) {
        // 削除処理
    	toDoService.deleteToDo(id);
        // フラッシュメッセージ
        attributes.addFlashAttribute("message", "ToDoが削除されました");
        // PRGパターン
        return "redirect:/todos";
    }
}