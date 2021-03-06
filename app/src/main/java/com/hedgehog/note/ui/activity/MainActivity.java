package com.hedgehog.note.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.hedgehog.note.R;
import com.hedgehog.note.adapter.BaseRecyclerviewAdapter;
import com.hedgehog.note.adapter.NoteAdapter;
import com.hedgehog.note.bean.Note;
import com.hedgehog.note.dao.NoteDao;
import com.hedgehog.note.event.NotifyEvent;
import com.hedgehog.note.ui.BaseApplication;
import com.lapism.searchview.adapter.SearchAdapter;
import com.lapism.searchview.adapter.SearchItem;
import com.lapism.searchview.history.SearchHistoryTable;
import com.lapism.searchview.view.SearchCodes;
import com.lapism.searchview.view.SearchView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.dao.query.Query;
import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    @Bind(R.id.recy_note)
    RecyclerView recyclerNote;
    @Bind(R.id.searchView)
    SearchView mSearchView;
    @Bind(R.id.text_none)
    TextView textNone;

    private Toolbar mToolbar;
    NoteAdapter noteAdapter;
    List<Note> notes;
    Query query;

    private SearchHistoryTable mHistoryDatabase;
    private List<SearchItem> mSuggestionsList;
    private int mVersion = SearchCodes.VERSION_MENU_ITEM;
    private int mStyle = SearchCodes.STYLE_MENU_ITEM_CLASSIC;
    private int mTheme = SearchCodes.THEME_LIGHT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        EventBus.getDefault().register(MainActivity.this);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));

        mToolbar.setTitle(" ");
        setSupportActionBar(mToolbar);

        query = getNoteDao().queryBuilder()
                .orderDesc(NoteDao.Properties.Date)
                .build();

        if (query.list().size() != 0) {
            initAdapter();
//      暂时没用了
//        noteAdapter.setOnInViewClickListener(R.id.note_more,
//                new BaseRecyclerviewAdapter.onInternalClickListenerImpl<Note>() {
//                    @Override
//                    public void OnClickListener(View parentV, View v, final Integer position, Note values) {
//                        super.OnClickListener(parentV, v, position, values);
//
//                        PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
//                        popupMenu.getMenuInflater().inflate(R.menu.item_popu_menu, popupMenu.getMenu());
//                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                            @Override
//                            public boolean onMenuItemClick(MenuItem item) {
//
//                                switch (item.getItemId()) {
//                                    case R.id.pop_del:
//
//                                        break;
//                                }
//                                return false;
//                            }
//                        });
//                        popupMenu.show();
//                    }
//                });

        }else{
            textNone.setVisibility(View.VISIBLE);
        }
        mHistoryDatabase = new SearchHistoryTable(this);
        mSuggestionsList = new ArrayList<>();

        mSearchView.setVersion(mVersion);
        mSearchView.setStyle(mStyle);
        mSearchView.setTheme(mTheme);
        mSearchView.setDivider(false);
        mSearchView.setHint("搜索笔记");
        mSearchView.setHintSize(getResources().getDimension(R.dimen.search_text_medium));
        mSearchView.setVoice(false);
        mSearchView.setAnimationDuration(300);
        mSearchView.setShadowColor(ContextCompat.getColor(this, R.color.search_shadow_layout));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String noteText) {
                mSearchView.hide(false);
                mHistoryDatabase.addItem(new SearchItem(noteText));
                if (!TextUtils.isEmpty(noteText)) {

                    Intent i=new Intent(MainActivity.this,SearchActivity.class);

                    i.putExtra("noteText",noteText);

                    startActivity(i);
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new SearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {

            }
        });

//      设置历史搜索部分
        List<SearchItem> mResultsList = new ArrayList<>();
        SearchAdapter mSearchAdapter = new SearchAdapter(this, mResultsList, mSuggestionsList, mTheme);
        mSearchAdapter.setOnItemClickListener(new SearchAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.textView_item_text);
                CharSequence text = textView.getText();
                mHistoryDatabase.addItem(new SearchItem(text));

                Intent i=new Intent(MainActivity.this,SearchActivity.class);
                i.putExtra("noteText",String.valueOf(text));
                startActivity(i);
                mSearchView.hide(true);
            }
        });

        mSearchView.setAdapter(mSearchAdapter);
    }

    private void initAdapter() {
        notes = (ArrayList) query.list();
        noteAdapter = new NoteAdapter(MainActivity.this, notes);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerNote.setLayoutManager(staggeredGridLayoutManager);
        recyclerNote.setAdapter(noteAdapter);

        noteAdapter.setOnInViewClickListener(R.id.note_content_ll, new BaseRecyclerviewAdapter.onInternalClickListenerImpl<Note>() {
            @Override
            public void OnClickListener(View parentV, View v, Integer position, Note values) {
                super.OnClickListener(parentV, v, position, values);
                Long id = notes.get(position).getId();
                Intent i = new Intent(MainActivity.this, NoteDetailActivity.class);
                i.putExtra("noteId", id);
                startActivity(i);
            }
        });
    }


    /**
     * @param event
     */
    public void onEventMainThread(NotifyEvent event) {
        Query query = getNoteDao().queryBuilder()
                .orderDesc(NoteDao.Properties.Date)
                .build();
        notes = query.list();
        switch (event.getType()) {
            case NotifyEvent.CREATE_NOTE:
            case NotifyEvent.UPDATE_NOTE:
            case NotifyEvent.DEL_NOTE:

                if(notes.size()==0){
                    textNone.setVisibility(View.VISIBLE);
                    noteAdapter.setList(notes);
                }else{
                    textNone.setVisibility(View.GONE);
                    if (noteAdapter == null) {
                        initAdapter();
                        noteAdapter.setList(notes);
                    } else {
                        noteAdapter.setList(notes);
                    }
                }
                break;

        }
    }

    @OnClick(R.id.fab_add_note)
    protected void addNote(View v) {
        switch (v.getId()) {
            case R.id.fab_add_note:
                startActivity(new Intent(this, AddNoteActivity.class));
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.item_menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_setting:
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
                break;
            case R.id.menu_search:
                showSearchView();
                break;
//          case R.id.item_about:
//              startActivity(new Intent(MainActivity.this, AboutActivity.class));
//              break;
            case R.id.item_recyclebin:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private NoteDao getNoteDao() {
        return ((BaseApplication) this.getApplicationContext()).getDaoSession().getNoteDao();
    }

    private void showSearchView() {
        mSuggestionsList.clear();
        mSuggestionsList.addAll(mHistoryDatabase.getAllItems());
        mSearchView.show(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(MainActivity.this);
    }
}
