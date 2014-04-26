
package hu.u_szeged.scannerpro.ui.fragments.views;


import java.util.List;

import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Customer;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.ui.fragments.components.CustomerThumbnailFragment;
import hu.u_szeged.scannerpro.ui.fragments.components.ReadingThumbnailFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;


/**
 * The fragment of the new scan view
 */
public class ScanFragment extends Fragment
{
	
	private boolean started = false;
	private Customer customer;
	
	public ScanFragment()
	{
	}
	
	// TODO: cameraview meghajtása: http://stackoverflow.com/a/10482872/301134
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.viewfragment_scan, container, false);
		
		rootView.findViewById(R.id.buttonScan).setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				int data = 42; // TODO: ide beírni a kiparseolt számot; a gomb csak akkor legyen engedélyezve, ha van mit parsolni és a customer != null
				
				Reading result = new Reading(data);		
				
				ScanResultFragment resultView = new ScanResultFragment();
				resultView.setCustomer(customer);
				resultView.setResult(result);
				
				final FragmentTransaction ft = getFragmentManager().beginTransaction(); 
				ft.replace(R.id.container, resultView); 
				ft.addToBackStack(null);
				ft.commit(); 
			}
		});		
			
		((AutoCompleteTextView) rootView.findViewById(R.id.autoCompleteTextViewCustomerId)).addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{
			}
			
			@Override
			public void afterTextChanged(Editable s)
			{
				if (started)
				{
					customer = DAO.FindCustomerById(s.toString());
					
					layoutCustomer();
				}
			}
		});
		
		
		return rootView;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		
		layoutCustomer();
		
		((AutoCompleteTextView) getView().findViewById(R.id.autoCompleteTextViewCustomerId))
			.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, DAO.GetAllCustomerIDs()));
		
		started = true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onStop()
	 */
	@Override
	public void onStop()
	{
		super.onStop();
		
		started = false;
	}
	
	/**
	 * Lays out the list of customers
	 */
	private void layoutCustomer()
	{
		LinearLayout panelCustomerInfo = (LinearLayout) getView().findViewById(R.id.linearLayoutCustomerInfo);
		
		panelCustomerInfo.removeAllViews();
		
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		if (customer != null)
		{
			CustomerThumbnailFragment customerThumbnail = new CustomerThumbnailFragment();
			customerThumbnail.setCustomer(customer);
			ft.add(R.id.linearLayoutCustomerInfo, customerThumbnail);
			
			List<Reading> readings = customer.getReadings();
			
			if (readings.size() > 0)
			{
				ReadingThumbnailFragment readingThumbnail = new ReadingThumbnailFragment();
				readingThumbnail.setReading(readings.get(readings.size() - 1));
				readingThumbnail.setHideCustomer(true);
				ft.add(R.id.linearLayoutCustomerInfo, readingThumbnail);
			}
			
			getView().findViewById(R.id.buttonScan).setEnabled(true);
		}
		else
		{
			getView().findViewById(R.id.buttonScan).setEnabled(false);
		}
		
		ft.commit();
	}
}
