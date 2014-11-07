package com.org.woody.woodylibrary.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.SparseArray;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

/**
 * Created by linfaxin on 2014/8/3 003.
 * Email: linlinfaxin@163.com
 * 绑定RadioGroup与Fragment，Fragment会在隐藏后被回收
 */
public abstract class RadioGroupStateFragmentBinder implements RadioGroup.OnCheckedChangeListener{
    private FragmentManager fm;
    private int containId;
    private SparseArray<Fragment> binded = new SparseArray<Fragment>();
    protected RadioGroupStateFragmentBinder(FragmentManager fm, int containId) {
        this.fm = fm;
        this.containId = containId;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if(!((CompoundButton)group.findViewById(checkedId)).isChecked()) return;//avoid check off callback
        if(fm==null) return;
        FragmentTransaction ft=fm.beginTransaction();
        
        Fragment fragment = getFragment(checkedId);
        ft.replace(containId, fragment).commit();
    }
    public void onChecked(int checkedId, Fragment fragment){

    }
    private Fragment getFragment(int checkedId){
        Fragment fragment = binded.get(checkedId);
        if(fragment==null){
            fragment = instanceFragment(checkedId);
            binded.put(checkedId, fragment);
        }
        return fragment;
    }
    public abstract Fragment instanceFragment(int checkedId);
}
