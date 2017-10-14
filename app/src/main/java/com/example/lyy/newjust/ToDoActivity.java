package com.example.lyy.newjust;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lyy.newjust.Model.Goal;
import com.example.lyy.newjust.db.ToDo;
import com.yalantis.beamazingtoday.interfaces.AnimationType;
import com.yalantis.beamazingtoday.interfaces.BatModel;
import com.yalantis.beamazingtoday.listeners.BatListener;
import com.yalantis.beamazingtoday.listeners.OnItemClickListener;
import com.yalantis.beamazingtoday.listeners.OnOutsideClickedListener;
import com.yalantis.beamazingtoday.ui.adapter.BatAdapter;
import com.yalantis.beamazingtoday.ui.animator.BatItemAnimator;
import com.yalantis.beamazingtoday.ui.callback.BatCallback;
import com.yalantis.beamazingtoday.ui.widget.BatRecyclerView;
import com.yalantis.beamazingtoday.util.TypefaceUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class ToDoActivity extends AppCompatActivity implements BatListener, OnItemClickListener, OnOutsideClickedListener {

    private BatRecyclerView mRecyclerView;
    private BatAdapter mAdapter;
    private List<BatModel> mGoals;
    private BatItemAnimator mAnimator;

    private static final String TAG = "MainActivity";

    private List<ToDo> toDos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        toDos = new ArrayList<>();

        ((TextView) findViewById(R.id.text_title)).setTypeface(TypefaceUtil.getAvenirTypeface(this));

        mRecyclerView = (BatRecyclerView) findViewById(R.id.bat_recycler_view);
        mAnimator = new BatItemAnimator();

        mRecyclerView.getView().setLayoutManager(new LinearLayoutManager(this));

        toDos = DataSupport.findAll(ToDo.class);

        Log.d(TAG, "onCreate: " + toDos.size());

        mRecyclerView.getView().setAdapter(mAdapter = new BatAdapter(mGoals = new ArrayList<BatModel>() {
            {
                for (ToDo toDo : toDos) {
                    Goal goal = new Goal(toDo.getContent());
                    goal.setChecked(toDo.isFinished());
                    Log.d(TAG, "instance initializer: " + toDo.getContent() + " " + toDo.isFinished());
                    add(goal);
                }
            }
        }, this, mAnimator).setOnItemClickListener(this).setOnOutsideClickListener(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new BatCallback(this));
        itemTouchHelper.attachToRecyclerView(mRecyclerView.getView());
        mRecyclerView.getView().setItemAnimator(mAnimator);
        mRecyclerView.setAddItemListener(this);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecyclerView.revertAnimation();
            }
        });
    }

    @Override
    public void add(String string) {
        mGoals.add(0, new Goal(string));
        Log.d(TAG, "add: " + mGoals.size());
        mAdapter.notify(AnimationType.ADD, 0);
    }

    @Override
    public void delete(int position) {
        mGoals.remove(position);
        Log.d(TAG, "delete: " + position);

        mAdapter.notify(AnimationType.REMOVE, position);
    }

    @Override
    public void move(int from, int to) {
        if (from >= 0 && to >= 0) {
            mAnimator.setPosition(to);
            BatModel model = mGoals.get(from);
            mGoals.remove(model);
            mGoals.add(to, model);
            mAdapter.notify(AnimationType.MOVE, from, to);

            if (from == 0 || to == 0) {
                mRecyclerView.getView().scrollToPosition(Math.min(from, to));
            }
        }
    }

    @Override
    public void onClick(BatModel item, int position) {
        Toast.makeText(this, item.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onOutsideClicked() {
        mRecyclerView.revertAnimation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + mGoals.size());
        DataSupport.deleteAll(ToDo.class);
        for (int i = 0; i < mGoals.size(); i++) {
            Log.d(TAG, "move: " + mGoals.get(i).getText() + mGoals.get(i).isChecked());
            ToDo todo = new ToDo();
            todo.setContent(mGoals.get(i).getText());
            todo.setFinished(mGoals.get(i).isChecked());
            todo.save();
        }
    }
}