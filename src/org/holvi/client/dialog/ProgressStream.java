package org.holvi.client.dialog;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for {@link InputStream} that reports the number of bytes read from the stream.
 */
public class ProgressStream extends FilterInputStream {
    private final PropertyChangeSupport propertyChange;
    private long bytesRead;
    
	protected ProgressStream(InputStream is, long maxNumBytes) {
		super(is);
        this.propertyChange = new PropertyChangeSupport(this);
	}


    public long getTotalNumBytesRead() {
        return bytesRead;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        propertyChange.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        propertyChange.removePropertyChangeListener(l);
    }

    @Override
    public int read() throws IOException {
        return (int)update(super.read());
    }

    @Override
    public int read(byte[] b) throws IOException {
        return (int)update(super.read(b));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return (int)update(super.read(b, off, len));
    }

    @Override
    public long skip(long n) throws IOException {
        return update(super.skip(n));
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    private long update(long numBytesRead) {
        if (numBytesRead > 0) {
            long oldTotalNumBytesRead = this.bytesRead;
            this.bytesRead += numBytesRead;
            propertyChange.firePropertyChange("totalNumBytesRead", oldTotalNumBytesRead, this.bytesRead);
        }

        return numBytesRead;
    }
}
