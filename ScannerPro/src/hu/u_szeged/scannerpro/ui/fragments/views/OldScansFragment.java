
package hu.u_szeged.scannerpro.ui.fragments.views;


import java.text.SimpleDateFormat;

import com.google.common.collect.Lists;

import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.ui.fragments.components.ReadingThumbnailFragment;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;


/**
 * The fragment of the old scans view
 */
public class OldScansFragment extends Fragment
{	
	private boolean started = false;
	private String filter = null;
	
	public OldScansFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.viewfragment_oldscans, container, false);
		
		((EditText) rootView.findViewById(R.id.editTextSearch)).addTextChangedListener(new TextWatcher() {
			
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
					filter = s.toString();
					
					if (filter != null && filter.equals(""))
					{
						filter = null;
					}
					
					layoutReadings();
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
		
		layoutReadings();
		
		started = true;
	}
	
	/* (non-Javadoc)
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
	private void layoutReadings()
	{
		LinearLayout panelReadingsList = (LinearLayout) getView().findViewById(R.id.panelReadingsList);
		
		panelReadingsList.removeAllViews();
		
		final FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		if (filter != null)
		{
			filter = filter.toLowerCase();
		}
		
		for (Reading reading : Lists.reverse(DAO.GetAllReadings()))
		{
			if (filter == null
					|| SimpleDateFormat.getDateTimeInstance().format(reading.getTimestamp()).contains(filter)
					|| (reading.getCustomer().getAddress() != null && reading.getCustomer().getAddress().toLowerCase().contains(filter))
					|| (reading.getCustomer().getId() != null && reading.getCustomer().getId().toLowerCase().contains(filter))
					|| (reading.getCustomer().getMeterId() != null && reading.getCustomer().getMeterId().toLowerCase().contains(filter))
					|| (reading.getCustomer().getMeterType() != null && reading.getCustomer().getMeterType().toLowerCase().contains(filter))
					|| (reading.getCustomer().getName() != null && reading.getCustomer().getName().toLowerCase().contains(filter)))
			{
				ReadingThumbnailFragment readingThumbnail = new ReadingThumbnailFragment();
				readingThumbnail.setReading(reading);
				ft.add(R.id.panelReadingsList, readingThumbnail);
			}
		}
		
		ft.commit();
	}
}
