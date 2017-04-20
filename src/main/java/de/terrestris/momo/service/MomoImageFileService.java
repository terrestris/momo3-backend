package de.terrestris.momo.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import de.terrestris.shogun2.dao.ImageFileDao;
import de.terrestris.shogun2.dao.UserDao;
import de.terrestris.shogun2.model.ImageFile;
import de.terrestris.shogun2.model.User;
import de.terrestris.shogun2.model.security.Permission;
import de.terrestris.shogun2.service.ImageFileService;
import de.terrestris.shogun2.service.UserService;

/**
*
* @author Johannes Weskamm
* @author terrestris GmbH & Co. KG
*
*/
@Service("momoImageFileService")
public class MomoImageFileService <E extends ImageFile, D extends ImageFileDao<E>>
	extends ImageFileService<E, D> {

	/**
	 * Default constructor, which calls the type-constructor
	 */
	@SuppressWarnings("unchecked")
	public MomoImageFileService() {
		this((Class<E>) ImageFile.class);
	}

	/**
	 * Constructor that sets the concrete entity class for the service.
	 * Subclasses MUST call this constructor.
	 */
	protected MomoImageFileService(Class<E> entityClass) {
		super(entityClass);
	}

	/**
	 * We have to use {@link Qualifier} to define the correct dao here.
	 * Otherwise, spring can not decide which dao has to be autowired here
	 * as there are multiple candidates.
	 */
	@Override
	@Autowired
	@Qualifier("imageFileDao")
	public void setDao(D dao) {
		this.dao = dao;
	}

	@Autowired
	@Qualifier("userService")
	private UserService<User, UserDao<User>> userService;

	/**
	 * Method override to persists a given Image with permission collection
	 *
	 * @param file
	 * @param createThumbnail
	 * @param thumbnailTargetSize
	 * @return
	 * @throws Exception
	 */
	@Override
	@PreAuthorize("isAuthenticated()")
	public E saveImage(MultipartFile file, boolean createThumbnail, Integer thumbnailTargetSize)
			throws Exception {

		InputStream is = null;
		ByteArrayInputStream bais = null;
		E imageToPersist = null;

		try {
			is = file.getInputStream();
			byte[] imageByteArray = IOUtils.toByteArray(is);

			// create a new instance (generic)
			imageToPersist = getEntityClass().newInstance();

			// create a thumbnail if requested
			if (createThumbnail) {
				byte[] thumbnail = scaleImage(
					imageByteArray,
					FilenameUtils.getExtension(file.getOriginalFilename()),
					thumbnailTargetSize);
				imageToPersist.setThumbnail(thumbnail);
			}

			// set binary image data
			imageToPersist.setFile(imageByteArray);

			// detect dimensions
			bais = new ByteArrayInputStream(imageByteArray);

			BufferedImage bimg = ImageIO.read(bais);

			// set basic image properties
			imageToPersist.setWidth(bimg.getWidth());
			imageToPersist.setHeight(bimg.getHeight());
			imageToPersist.setFileType(file.getContentType());
			imageToPersist.setFileName(file.getOriginalFilename());

			// persist the image
			dao.saveOrUpdate(imageToPersist);

			User user = userService.getUserBySession();
			if (user != null) {
				addAndSaveUserPermissions(imageToPersist, user, Permission.ADMIN);
			}

		} catch(Exception e) {
			throw new Exception("Could not create the Image in DB: "
					+ e.getMessage());
		} finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(bais);
		}

		return imageToPersist;
	}
}
