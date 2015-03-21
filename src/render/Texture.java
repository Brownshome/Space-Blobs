package render;

import io.user.Console;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {
	public int width;
	public int height;
	public ByteBuffer data;

	/** data is in GL_UNSIGNED_BYTE form */
	public Texture(String name, Format format) {
		try (InputStream in = new FileInputStream("textures/" + name + ".png")) {
			PNGDecoder decoder = new PNGDecoder(in);
			ByteBuffer buf = BufferUtils.createByteBuffer(format.getNumComponents() * decoder.getWidth() * decoder.getHeight());
			decoder.decode(buf, decoder.getWidth() * format.getNumComponents(), format);
			buf.flip();

			data = buf;
			width = decoder.getWidth();
			height = decoder.getHeight();

		} catch (IOException e) {
			Console.error("IO Error in texture read", e, "IO");
		}
	}
}
