package com.boardgamegeek.ui.viewmodel

import android.app.Application
import androidx.core.content.contentValuesOf
import androidx.lifecycle.*
import androidx.palette.graphics.Palette
import com.boardgamegeek.entities.RefreshableResource
import com.boardgamegeek.extensions.asDateForApi
import com.boardgamegeek.extensions.getHeaderSwatch
import com.boardgamegeek.extensions.isOlderThan
import com.boardgamegeek.livedata.Event
import com.boardgamegeek.provider.BggContract
import com.boardgamegeek.repository.GameCollectionRepository
import com.boardgamegeek.util.RemoteConfig
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class GameCollectionItemViewModel(application: Application) : AndroidViewModel(application) {
    private val gameCollectionRepository = GameCollectionRepository(getApplication())
    private val areItemsRefreshing = AtomicBoolean()
    private val isImageRefreshing = AtomicBoolean()
    private val refreshMinutes = RemoteConfig.getInt(RemoteConfig.KEY_REFRESH_GAME_COLLECTION_MINUTES)

    private val _collectionId = MutableLiveData<Int>()
    val collectionId: LiveData<Int>
        get() = _collectionId

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<Event<String>> = Transformations.map(_errorMessage) { Event(it) }

    val isEdited = MutableLiveData<Boolean>()

    init {
        isEdited.value = false
    }

    fun setId(id: Int) {
        if (_collectionId.value != id) _collectionId.value = id
    }

    val item = _collectionId.switchMap { id ->
        liveData {
            val item = gameCollectionRepository.loadCollectionItem(id)
            val refreshedItem =
                if (areItemsRefreshing.compareAndSet(false, true)) {
                    val gameId = item?.gameId ?: BggContract.INVALID_ID
                    val lastUpdated = item?.syncTimestamp ?: 0L
                    when {
                        lastUpdated.isOlderThan(refreshMinutes, TimeUnit.MINUTES) -> {
                            emit(RefreshableResource.refreshing(item))
                            gameCollectionRepository.refreshCollectionItem(gameId, id)
                        }
                        else -> item
                    }.also { areItemsRefreshing.set(false) }
                } else item
            val itemWithImage = if (isImageRefreshing.compareAndSet(false, true)) {
                refreshedItem?.let {
                    gameCollectionRepository.refreshHeroImage(it)
                }.also { isImageRefreshing.set(false) }
            } else refreshedItem
            emit(RefreshableResource.success(itemWithImage))
        }
    }

    private val _swatch = MutableLiveData<Palette.Swatch>()
    val swatch: LiveData<Palette.Swatch>
        get() = _swatch

    fun refresh() {
        _collectionId.value?.let { _collectionId.value = it }
    }

    fun updateGameColors(palette: Palette?) {
        palette?.let { _swatch.value = it.getHeaderSwatch() }
    }

    fun updatePrivateInfo(
        priceCurrency: String?,
        price: Double?,
        currentValueCurrency: String?,
        currentValue: Double?,
        quantity: Int?,
        acquisitionDate: Long?,
        acquiredFrom: String?,
        inventoryLocation: String?
    ) {
        setEdited(true)
        val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()

        // TODO inspect to ensure something changed
        val values = contentValuesOf(
            BggContract.Collection.PRIVATE_INFO_DIRTY_TIMESTAMP to System.currentTimeMillis(),
            BggContract.Collection.PRIVATE_INFO_PRICE_PAID_CURRENCY to priceCurrency,
            BggContract.Collection.PRIVATE_INFO_PRICE_PAID to price,
            BggContract.Collection.PRIVATE_INFO_CURRENT_VALUE_CURRENCY to currentValueCurrency,
            BggContract.Collection.PRIVATE_INFO_CURRENT_VALUE to currentValue,
            BggContract.Collection.PRIVATE_INFO_QUANTITY to quantity,
            BggContract.Collection.PRIVATE_INFO_ACQUISITION_DATE to acquisitionDate.asDateForApi(),
            BggContract.Collection.PRIVATE_INFO_ACQUIRED_FROM to acquiredFrom,
            BggContract.Collection.PRIVATE_INFO_INVENTORY_LOCATION to inventoryLocation
        )
        gameCollectionRepository.update(internalId, values)
    }

    fun updateStatuses(statuses: List<String>, wishlistPriority: Int) {
        setEdited(true)
        val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()
        // TODO inspect to ensure something changed
        val values = contentValuesOf(
            BggContract.Collection.STATUS_DIRTY_TIMESTAMP to System.currentTimeMillis(),
            BggContract.Collection.STATUS_OWN to statuses.contains(BggContract.Collection.STATUS_OWN),
            BggContract.Collection.STATUS_PREVIOUSLY_OWNED to statuses.contains(BggContract.Collection.STATUS_PREVIOUSLY_OWNED),
            BggContract.Collection.STATUS_PREORDERED to statuses.contains(BggContract.Collection.STATUS_PREORDERED),
            BggContract.Collection.STATUS_FOR_TRADE to statuses.contains(BggContract.Collection.STATUS_FOR_TRADE),
            BggContract.Collection.STATUS_WANT to statuses.contains(BggContract.Collection.STATUS_WANT),
            BggContract.Collection.STATUS_WANT_TO_BUY to statuses.contains(BggContract.Collection.STATUS_WANT_TO_BUY),
            BggContract.Collection.STATUS_WANT_TO_PLAY to statuses.contains(BggContract.Collection.STATUS_WANT_TO_PLAY),
            BggContract.Collection.STATUS_WISHLIST to statuses.contains(BggContract.Collection.STATUS_WISHLIST)
        )
        if (statuses.contains(BggContract.Collection.STATUS_WISHLIST)) {
            values.put(BggContract.Collection.STATUS_WISHLIST_PRIORITY, wishlistPriority.coerceIn(1..5))
        }
        gameCollectionRepository.update(internalId, values)
    }

    fun updateRating(rating: Double) {
        val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()
        val currentRating = item.value?.data?.rating ?: 0.0
        if (rating != currentRating) {
            setEdited(true)
            val values = contentValuesOf(
                BggContract.Collection.RATING to rating,
                BggContract.Collection.RATING_DIRTY_TIMESTAMP to System.currentTimeMillis()
            )
            gameCollectionRepository.update(internalId, values)
        }
    }

    fun updateText(text: String, textColumn: String, timestampColumn: String, originalText: String? = null) {
        if (text != originalText) {
            setEdited(true)
            val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()
            val values = contentValuesOf(
                textColumn to text,
                timestampColumn to System.currentTimeMillis()
            )
            gameCollectionRepository.update(internalId, values)
        }
    }

    fun delete() {
        setEdited(false)
        val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()
        val values = contentValuesOf(BggContract.Collection.COLLECTION_DELETE_TIMESTAMP to System.currentTimeMillis())
        gameCollectionRepository.update(internalId, values)
    }

    fun reset() {
        setEdited(false)
        val internalId = item.value?.data?.internalId ?: BggContract.INVALID_ID.toLong()
        val gameId = item.value?.data?.gameId ?: BggContract.INVALID_ID
        gameCollectionRepository.resetTimestamps(internalId, gameId, _errorMessage)
    }

    private fun setEdited(edited: Boolean) {
        if (isEdited.value != edited) isEdited.value = edited
    }
}
