package hu.u_szeged.scannerpro.ui.fragments.views;

import hu.u_szeged.scannerpro.R;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

/**
 * The fragment of the main view
 */
public class MainFragment extends Fragment {

	public MainFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.viewfragment_main, container, false);
		
		rootView.findViewById(R.id.buttonNewScan).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				ft.replace(R.id.container, new ScanFragment()); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});
		
		rootView.findViewById(R.id.buttonOldScans).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				ft.replace(R.id.container, new OldScansFragment()); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});
		
		rootView.findViewById(R.id.buttonCustomers).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				ft.replace(R.id.container, new CustomersFragment()); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});
		
		return rootView;
	}
}