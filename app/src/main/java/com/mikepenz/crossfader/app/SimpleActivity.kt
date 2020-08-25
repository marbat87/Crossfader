package com.mikepenz.crossfader.app

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.mikepenz.crossfader.Crossfader
import com.mikepenz.crossfader.app.util.CrossfadeWrapper
import com.mikepenz.crossfader.util.UIUtils
import com.mikepenz.crossfader.view.CrossFadeSlidingPaneLayout
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.materialdrawer.*
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.interfaces.Nameable
import com.mikepenz.materialize.MaterializeBuilder
import com.mikepenz.materialize.color.Material

class SimpleActivity : AppCompatActivity() {
    //save our header or result
    private lateinit var headerResult: AccountHeader
    private lateinit var result: Drawer
    private lateinit var miniResult: MiniDrawer
    private lateinit var crossFader: Crossfader<*>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple)

        // Handle Toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            //set the back arrow in the toolbar
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        //handle the style
        MaterializeBuilder(this).build()

        // Create a few sample profile
        // NOTE you have to define the loader logic too. See the CustomApplication for more details
        val profile = ProfileDrawerItem().withName("Mike Penz").withEmail("mikepenz@gmail.com").withIcon("https://avatars3.githubusercontent.com/u/1476232?v=3&s=460")
        val profile2 = ProfileDrawerItem().withName("Bernat Borras").withEmail("alorma@github.com").withIcon(Uri.parse("https://avatars3.githubusercontent.com/u/887462?v=3&s=460"))
        // Create the AccountHeader
        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withHeaderBackground(ColorDrawable(Material.Blue._900.asColor))
                .addProfiles(
                        profile,
                        profile2
                )
                .withOnAccountHeaderListener { _, _, _ ->
                    miniResult.onProfileClick()
                    false
                }
                .withSavedInstance(savedInstanceState)
                .build()
        val builder: DrawerBuilder = DrawerBuilder()
                .withActivity(this)
                .withTranslucentStatusBar(false)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        PrimaryDrawerItem().withName(R.string.drawer_item_first).withIcon(GoogleMaterial.Icon.gmd_3d_rotation).withIdentifier(1),
                        PrimaryDrawerItem().withName(R.string.drawer_item_second).withIcon(FontAwesome.Icon.faw_home).withIdentifier(2),
                        PrimaryDrawerItem().withName(R.string.drawer_item_third).withIcon(FontAwesome.Icon.faw_gamepad).withIdentifier(3),
                        PrimaryDrawerItem().withName(R.string.drawer_item_fourth).withIcon(FontAwesome.Icon.faw_eye).withIdentifier(4),
                        PrimaryDrawerItem().withDescription("A more complex sample").withName(R.string.drawer_item_fifth).withIcon(GoogleMaterial.Icon.gmd_adb).withIdentifier(5),
                        PrimaryDrawerItem().withName(R.string.drawer_item_sixth).withIcon(GoogleMaterial.Icon.gmd_camera).withIdentifier(6),
                        DividerDrawerItem(),
                        SecondaryDrawerItem().withName(R.string.drawer_item_seventh).withIcon(FontAwesome.Icon.faw_github)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener { _, _, drawerItem ->
                    if (drawerItem is Nameable<*>) {
                        Toast.makeText(this@SimpleActivity, (drawerItem as Nameable<*>).name!!.getText(this@SimpleActivity), Toast.LENGTH_SHORT).show()
                    }
                    miniResult.onItemClick(drawerItem)
                    true
                }
                .withSavedInstance(savedInstanceState)

        // build only the view of the Drawer (don't inflate it automatically in our layout which is done with .build())
        result = builder.buildView()
        // create the MiniDrawer and deinfe the drawer and header to be used (it will automatically use the items from them)
        miniResult = MiniDrawer().withDrawer(result).withAccountHeader(headerResult)


        //IMPORTANT Crossfader specific implementation starts here (everything above is MaterialDrawer):

        //get the widths in px for the first and second panel
        val firstWidth = UIUtils.convertDpToPixel(200f, this).toInt()
        val secondWidth = UIUtils.convertDpToPixel(72f, this).toInt()

        //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
        crossFader = Crossfader<CrossFadeSlidingPaneLayout>()
                .withContent(findViewById(R.id.crossfade_content))
                .withFirst(result.slider, firstWidth)
                .withSecond(miniResult.build(this), secondWidth)
                .withGmailStyleSwiping()
                .withSavedInstance(savedInstanceState)
                .build()

        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
        miniResult.withCrossFader(CrossfadeWrapper(crossFader))

        //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
        crossFader.crossFadeSlidingPaneLayout.setShadowResourceLeft(R.drawable.material_drawer_shadow_left)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        //add the values which need to be saved from the drawer to the bundle
        var mOutState = result.saveInstanceState(outState)
        //add the values which need to be saved from the accountHeader to the bundle
        mOutState = headerResult.saveInstanceState(mOutState)
        //add the values which need to be saved from the crossFader to the bundle
        mOutState = crossFader.saveInstanceState(mOutState)
        super.onSaveInstanceState(mOutState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.menu_1).icon = IconicsDrawable(this, GoogleMaterial.Icon.gmd_sort).color(Color.WHITE).actionBar()
        return true
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result.isDrawerOpen) {
            result.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //handle the click on the back arrow click
        return when (item.itemId) {
            R.id.menu_1 -> {
                crossFader.crossFade()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}