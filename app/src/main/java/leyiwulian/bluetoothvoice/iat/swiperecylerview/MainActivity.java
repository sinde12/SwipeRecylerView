package leyiwulian.bluetoothvoice.iat.swiperecylerview;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private SwipeRecyclerView recyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(new IAdapter());
    }

    private class IAdapter extends RecyclerView.Adapter<IAdapter.IHolder> implements ISwipeRecyclerViewAdapter{

        private List<String> datas = new ArrayList<>();
        IAdapter(){
            for (int i =0;i<50;i++){
                datas.add(String.format("item view %s",i));
            }
        }

        @NonNull
        @Override
        public IHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            return new IHolder(View.inflate(MainActivity.this,R.layout.adapter_list2,null));
        }

        @Override
        public void onBindViewHolder(@NonNull IHolder iHolder, int i) {
            iHolder.contentTV.setText(datas.get(i));
            iHolder.position = i;
        }

        @Override
        public int getItemCount() {
            return datas.size();
        }

        @Override
        public int leftFunctionWidth(int position) {
            return 50;
        }

        @Override
        public int rightFunctionWidth(int position) {
            return 100;
        }

        class IHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

            private TextView contentTV;
            private int position = 0;
            IHolder(View v){
                super(v);
                RecyclerView.LayoutParams lp= new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT,(int)(getResources().getDisplayMetrics().density*70));
                v.setLayoutParams(lp);
                contentTV = v.findViewById(R.id.tv);
                v.findViewById(R.id.editTV).setOnClickListener(this);
                v.findViewById(R.id.deleteTV).setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                switch (view.getId()){
                    case R.id.editTV:{
                        recyclerView.endEditing();
                    }
                        break;
                    case R.id.deleteTV:{
                        recyclerView.endEditing();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                datas.remove(position);
                                notifyDataSetChanged();
                            }
                        },1000);
                    }
                        break;
                }
            }
        }

    }

}
