package com.example.todo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.todo.entity.ToDo;
import com.example.todo.mapper.ToDoMapper;

import lombok.RequiredArgsConstructor;

/**
 * ToDo：サービスクラス
 */
@Service
@Transactional
@RequiredArgsConstructor
public class ToDoService {

    /** DI */
    private final ToDoMapper toDoMapper;
    
    /**
     * 全「すること」を検索します。
     */
    public List<ToDo> findAllToDo() {
        return toDoMapper.selectAll();
    }

    /**
     * 指定されたIDの「すること」を検索します。
     */
    public ToDo findByIdToDo(Integer id) {
        return toDoMapper.selectById(id);
    }

    /**
     * 「すること」を新規登録します。
     */
    public void insertToDo(ToDo toDo) {
        toDoMapper.insert(toDo);
    }

    /**
     * 「すること」を更新します。
     */
    public void updateToDo(ToDo toDo) {
        toDoMapper.update(toDo);
    }

    /**
     * 指定されたIDの「すること」を削除します。
     */
    public void deleteToDo(Integer id) {
        toDoMapper.delete(id);
    }
}