
package hu.u_szeged.scannerpro.ui.fragments.components;


import java.text.SimpleDateFormat;

import hu.u_szeged.scannerpro.R;
import hu.u_szeged.scannerpro.model.DAO;
import hu.u_szeged.scannerpro.model.beans.Reading;
import hu.u_szeged.scannerpro.ui.fragments.views.ViewCustomerFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * The fragment of the thumbnail of a customer
 */
public class ReadingThumbnailFragment extends Fragment
{
	
	private Reading reading;
	private boolean hideCustomer;
	
	public ReadingThumbnailFragment()
	{
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View rootView = inflater.inflate(R.layout.componentfragment_thumbnail_reading, container, false);
		
		rootView.findViewById(R.id.buttonViewCustomer).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				final FragmentTransaction ft = getFragmentManager().beginTransaction();
				ViewCustomerFragment view = new ViewCustomerFragment();
				view.setCustomer(reading.getCustomer());
				ft.replace(R.id.container, view);
				ft.addToBackStack(null);
				ft.commit();
			}
		});
		
		rootView.findViewById(R.id.buttonDeleteReading).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v)
			{
				new AlertDialog.Builder(getActivity())
						.setTitle("Óraállás törlése")
						.setMessage("Biztosan törli ezt az óraállást?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								reading.getCustomer().removeReading(reading);
								DAO.Commit();
								getActivity().getSupportFragmentManager().beginTransaction().remove(ReadingThumbnailFragment.this).commit();
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								// do nothing
							}
						})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show();
			}
		});
		
		return rootView;
	}
	
	@Override
	public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState)
	{
		super.onInflate(activity, attrs, savedInstanceState);
		
		TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.ReadingThumbnailFragment);
		
		setHideCustomer(a.getBoolean(R.styleable.ReadingThumbnailFragment_hide_customer, false));
		
		a.recycle();
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		
		layoutReading();
	}
	
	
	/**
	 * @return the reading being displayed
	 */
	public Reading getReading()
	{
		return reading;
	}
	
	
	/**
	 * @param reading the reading to display (not null)
	 */
	public void setReading(Reading reading)
	{
		this.reading = reading;
		
		layoutReading();
	}
	
	/**
	 * Sets whether the customer display should be hidden
	 * 
	 * @param hideCustomer
	 */
	public void setHideCustomer(boolean hideCustomer)
	{
		this.hideCustomer = hideCustomer;
		
		layoutReading();
	}
	
	private void layoutReading()
	{
		if (getView() == null || reading == null)
		{
			return;
		}
		
		((TextView) getView().findViewById(R.id.textViewReadingTimestamp)).setText("Leolvasás ideje: " + SimpleDateFormat.getDateTimeInstance().format(reading.getTimestamp()));
		((TextView) getView().findViewById(R.id.textViewReadingValue)).setText("Óraállás: " + reading.getReading());
		
		if (!hideCustomer)
		{
			((TextView) getView().findViewById(R.id.textViewCustomerId)).setText(reading.getCustomer().getId());
			((TextView) getView().findViewById(R.id.textViewCustomerName)).setText(reading.getCustomer().getName());
			((TextView) getView().findViewById(R.id.textViewCustomerAddress)).setText(reading.getCustomer().getAddress());
			
			getView().findViewById(R.id.buttonViewCustomer).setVisibility(View.VISIBLE);
			getView().findViewById(R.id.linearLayoutCustomer).setVisibility(View.VISIBLE);
		}
		else
		{
			getView().findViewById(R.id.buttonViewCustomer).setVisibility(View.GONE);
			getView().findViewById(R.id.linearLayoutCustomer).setVisibility(View.GONE);
			// TODO: data panel kiszélesítése
		}
	}
}
