
package com.bhsoft.ar3d.ui.main.camera_detect_activity.common.samplerender

import android.opengl.GLES30
import java.nio.Buffer

/* package-private */
internal class GpuBuffer(target: Int, numberOfBytesPerEntry: Int, entries: Buffer?) {
    private val target: Int
    private val numberOfBytesPerEntry: Int
    private val bufferId = intArrayOf(0)
    var size = 0
        private set
    private var capacity = 0
    fun set(entries: Buffer?) {
        // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
        // called with a size of 0, so avoid this case.
        if (entries == null || entries.limit() == 0) {
            size = 0
            return
        }
        require(entries.isDirect) { "If non-null, entries buffer must be a direct buffer" }
        GLES30.glBindBuffer(target, bufferId[0])
        entries.rewind()
        if (entries.limit() <= capacity) {
            GLES30.glBufferSubData(target, 0, entries.limit() * numberOfBytesPerEntry, entries)
            size = entries.limit()
        } else {
            GLES30.glBufferData(
                target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW
            )
            size = entries.limit()
            capacity = entries.limit()
        }
    }

    fun free() {
        if (bufferId[0] != 0) {
            GLES30.glDeleteBuffers(1, bufferId, 0)
            bufferId[0] = 0
        }
    }

    fun getBufferId(): Int {
        return bufferId[0]
    }

    companion object {
        private val TAG = GpuBuffer::class.java.simpleName

        // These values refer to the byte count of the corresponding Java datatypes.
        const val INT_SIZE = 4
        const val FLOAT_SIZE = 4
    }

    init {
        var entries = entries
        if (entries != null) {
            require(entries.isDirect) { "If non-null, entries buffer must be a direct buffer" }
            // Some GPU drivers will fail with out of memory errors if glBufferData or glBufferSubData is
            // called with a size of 0, so avoid this case.
            if (entries.limit() == 0) {
                entries = null
            }
        }
        this.target = target
        this.numberOfBytesPerEntry = numberOfBytesPerEntry
        if (entries == null) {
            size = 0
            capacity = 0
        } else {
            size = entries.limit()
            capacity = entries.limit()
        }
        try {
            // Clear VAO to prevent unintended state change.
            GLES30.glBindVertexArray(0)
            GLES30.glGenBuffers(1, bufferId, 0)
            GLES30.glBindBuffer(target, bufferId[0])
            if (entries != null) {
                entries.rewind()
                GLES30.glBufferData(
                    target, entries.limit() * numberOfBytesPerEntry, entries, GLES30.GL_DYNAMIC_DRAW
                )
            }
        } catch (t: Throwable) {
            free()
            throw t
        }
    }
}