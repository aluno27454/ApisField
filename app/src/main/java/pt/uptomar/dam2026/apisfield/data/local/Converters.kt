package pt.uptomar.dam2026.apisfield.data.local

import androidx.room.TypeConverter
import java.time.Instant

/**
 * Conversões entre tipos que o SQLite não guarda diretamente e tipos que guarda.
 *
 * O SQLite só conhece números, texto e blobs. As datas ([Instant]) são guardadas como o número
 * de milissegundos desde 1970 (UTC), o que permite ordenar por data diretamente no SQL.
 * Os enums não precisam de conversor: o Room guarda-os pelo nome (ex.: `"PENDENTE"`).
 *
 * Referência: https://developer.android.com/training/data-storage/room/referencing-data
 */
class Converters {

    @TypeConverter
    fun instantParaMillis(data: Instant?): Long? = data?.toEpochMilli()

    @TypeConverter
    fun millisParaInstant(millis: Long?): Instant? = millis?.let(Instant::ofEpochMilli)
}
