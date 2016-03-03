package com.ieeton.agency.net;

import internal.org.apache.http.entity.mime.HttpMultipartMode;
import internal.org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class CustomMultiPartEntity extends MultipartEntity {

	private final IDownloadState listener;

	public CustomMultiPartEntity(final IDownloadState listener) {
		super();
		this.listener = listener;
	}

	public CustomMultiPartEntity(final HttpMultipartMode mode,
			final IDownloadState listener) {
		super(mode);
		this.listener = listener;
	}

	public CustomMultiPartEntity(HttpMultipartMode mode, final String boundary,
			final Charset charset, final IDownloadState listener) {
		super(mode, boundary, charset);
		this.listener = listener;
	}

	@Override
	public void writeTo(final OutputStream outstream) throws IOException {
		super.writeTo(new CountingOutputStream(outstream, this.listener,
				getContentLength()));
	}

//	@Override
//	public boolean isRepeatable() {
//		return super.isRepeatable();
//	}

	public static class CountingOutputStream extends FilterOutputStream {

		private final IDownloadState listener;
		private long transferred;
		private long mContentLength;

		public CountingOutputStream(final OutputStream out,
				final IDownloadState listener, final long contentLength) {
			super(out);
			this.listener = listener;
			this.transferred = 0;
			this.mContentLength = contentLength;
//			Utils.loge("mContentLength:"+mContentLength+" hashcode:"+hashCode());
		}

		public void write(byte[] b, int off, int len) throws IOException {
//			Utils.loge("transferred:"+transferred+" len:"+len+" hashcode:"+hashCode());
			
			out.write(b, off, len);
			out.flush();
			if (len > 0) {
				this.transferred += len;
				 
				this.listener
						.onProgressChanged((this.transferred * 100 / mContentLength));
			}
		}

		public void write(int b) throws IOException {
			out.write(b);
			out.flush();
			this.transferred++;
		}
	}
}