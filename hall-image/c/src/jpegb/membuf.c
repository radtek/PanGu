/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. NIST assumes no responsibility  whatsoever for 
its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. 

Disclaimer: 
This software was developed to promote biometric standards and biometric
technology testing for the Federal Government in accordance with the USA
PATRIOT Act and the Enhanced Border Security and Visa Entry Reform Act.
Specific hardware and software products identified in this software were used
in order to perform the software development.  In no case does such
identification imply recommendation or endorsement by the National Institute
of Standards and Technology, nor does it imply that the products and equipment
identified are necessarily the best available for the purpose.  

*******************************************************************************/

/***********************************************************************
      LIBRARY: JPEGB - Baseline (Lossy) JPEG Utilities

      FILE:    MEMBUF.C
      AUTHORS: Michael Garris
      DATE:    01/05/2001

      Contains data destination and data source managers used by
      the Baseline JPEG encoder/decoder.  The routines provide a
      low-level interface for writing and reading data using a
      memory buffer.

      ROUTINES:
#cat: jpeg_membuf_dest - JPEGB destination manager designed to write
#cat:                    compression results to a memory buffer.
#cat: jpeg_membuf_src - JPEGB source manager designed to read compressed
#cat:                    data from a memory buffer.

***********************************************************************/

/*
 * membuf.c
 *
 * Author: Michael D. Garris
 *         NIST
 * Date:   January 5, 2001
 * This file is based in part on files jdatadst.c and jdatasrc.c
 * distributed as part of the Independent JPEG Group's software.
 *
 * This file contains compression data destination routines for the case of
 * emitting JPEG data to a memory buffer.  This file also contains
 * decompression data source routines for the case of reading JPEG data
 * from a memory buffer.
 * IMPORTANT: we assume that the memory buffer is an array of JOCTETs
 * from 8-bit-wide elements.
 */

/* this is not a core library module, so it doesn't define JPEG_INTERNALS */
#include "../include/jinclude.h"
#include "../include/jpeglib.h"
#include "../include/jerror.h"


/* Expanded data destination object for stdio output */

typedef struct {
  struct jpeg_destination_mgr pub; /* public fields */

  JOCTET  *out_buffer;		/* start of output buffer */
  size_t   out_buffer_size;     /* size (in bytes) of output buffer */
} my_destination_mgr;

typedef my_destination_mgr * my_dest_ptr;


/* Expanded data source object for stdio input */

typedef struct {
  struct jpeg_source_mgr pub;	/* public fields */

  JOCTET  *in_buffer;		/* start of input buffer */
  size_t   in_buffer_size;      /* size (in bytes) of input buffer */
} my_source_mgr;

typedef my_source_mgr * my_src_ptr;


/* START DESTINTATION MANAGER ROUTINES */

/*
 * Initialize destination --- called by jpeg_start_compress
 * before any data is actually written.
 */

METHODDEF(void)
init_destination (j_compress_ptr cinfo)
{
  my_dest_ptr dest = (my_dest_ptr) cinfo->dest;

  /* The output buffer must already be allocated and then set, */
  /* for example, by jpeg_membuf_dest(). */

  /* Set next_output_byte to beginning of output buffer. */
  dest->pub.next_output_byte = dest->out_buffer;
  /* Set free_in_buffer to size of output buffer. */
  dest->pub.free_in_buffer = dest->out_buffer_size;
}


/*
 * Empty the output buffer --- called whenever buffer fills up.
 *
 * In the case of decompressed data being stored to one large
 * output memory buffer, if the buffer fills up, this is an
 * error condition.  The output buffer should be allocated the
 * size of the expected reconstructed image pixmap based on the
 * dimensions and pixel depth of the compressed image.
 */

METHODDEF(boolean)
empty_output_buffer (j_compress_ptr cinfo)
{
  ERREXIT(cinfo, JERR_BUFFER_SIZE);

  return TRUE;
}


/*
 * Terminate destination --- called by jpeg_finish_compress
 * after all data has been written.  In this case we simply
 * want the contents of the output buffer passed back, so no-op.
 */

METHODDEF(void)
term_destination (j_compress_ptr cinfo)
{
  /* no work necessary here */
}

/*
 * Prepare for output to a memory buffer.
 * The caller must have already allocated the buffer, and is responsible
 * for deallocating it after finishing compression.
 */

GLOBAL(void)
jpeg_membuf_dest (j_compress_ptr cinfo,
                 JOCTET *out_buffer, size_t out_buffer_size)
{
  my_dest_ptr dest;

  /* The destination object is made permanent, but it should only be used
   * repetitively in the case where multiple JPEG images are guaranteed to
   * be equal or smaller to the out_buffer_size.
   */
  if (cinfo->dest == NULL) {	/* first time for this JPEG object? */
    cinfo->dest = (struct jpeg_destination_mgr *)
      (*cinfo->mem->alloc_small) ((j_common_ptr) cinfo, JPOOL_PERMANENT,
				  SIZEOF(my_destination_mgr));
  }

  dest = (my_dest_ptr) cinfo->dest;
  dest->pub.init_destination = init_destination;
  dest->pub.empty_output_buffer = empty_output_buffer;
  dest->pub.term_destination = term_destination;
  dest->out_buffer = out_buffer;
  dest->out_buffer_size = out_buffer_size;
}



/* START SOURCE MANAGER ROUTINES */

/*
 * Initialize source --- called by jpeg_read_header
 * before any data is actually read.
 */

METHODDEF(void)
init_source (j_decompress_ptr cinfo)
{
  my_src_ptr src = (my_src_ptr) cinfo->src;

  /* The input buffer must already be allocated, filled, and then set, */
  /* for example, by jpeg_membuf_src(). */

  /* Set next_input_byte to beginning of input buffer. */
  src->pub.next_input_byte = src->in_buffer;
  /* Set bytes_in_buffer to size of input buffer. */
  src->pub.bytes_in_buffer = src->in_buffer_size;
}


/*
 * Fill the input buffer --- called whenever buffer is emptied.
 *
 * In the case of compressed data being read from one large
 * input memory buffer, if the buffer becomes empty prior to
 * completing the decompression, this is an error condition.
 */

METHODDEF(boolean)
fill_input_buffer (j_decompress_ptr cinfo)
{
  ERREXIT(cinfo, JERR_TOO_LITTLE_DATA);
  return TRUE;
}


/*
 * Skip data --- used to skip over a potentially large amount of
 * uninteresting data (such as an APPn marker).
 */

METHODDEF(void)
skip_input_data (j_decompress_ptr cinfo, long num_bytes)
{
  my_src_ptr src = (my_src_ptr) cinfo->src;

  /* If the size of request jump is larger than remaining bytes in */
  /* input buffer, then error condition. */
  if(num_bytes > (long)(src->pub.bytes_in_buffer))
    ERREXIT(cinfo, JERR_BUFFER_SIZE);

  /* Bump next_input_byte forward in buffer. */
  src->pub.next_input_byte += (size_t) num_bytes;
  /* Decrement bytes_in_buffer by size of jump. */
  src->pub.bytes_in_buffer -= (size_t) num_bytes;
}

/*
 * An additional method that can be provided by data source modules is the
 * resync_to_restart method for error recovery in the presence of RST markers.
 * For the moment, this source module just uses the default resync method
 * provided by the JPEG library.  That method assumes that no backtracking
 * is possible.
 */


/*
 * Terminate source --- called by jpeg_finish_decompress
 * after all data has been read.  In this case we simply
 * leave the input buffer alone, so no-op.
 */

METHODDEF(void)
term_source (j_decompress_ptr cinfo)
{
  /* no work necessary here */
}


/*
 * Prepare for input from a memory buffer.
 * The caller must have already allocated and filled the buffer, and
 * is responsible for deallocating it after finishing decompression.
 */

GLOBAL(void)
jpeg_membuf_src (j_decompress_ptr cinfo,
                 JOCTET *in_buffer, size_t in_buffer_size)
{
  my_src_ptr src;

  /* The source object is made permanent, but it should only be used
   * repetitively in the case where multiple JPEG images are guaranteed
   * to be equal or smaller to the in_buffer_size.
   */
  if (cinfo->src == NULL) {	/* first time for this JPEG object? */
    cinfo->src = (struct jpeg_source_mgr *)
      (*cinfo->mem->alloc_small) ((j_common_ptr) cinfo, JPOOL_PERMANENT,
				  SIZEOF(my_source_mgr));
  }

  src = (my_src_ptr) cinfo->src;
  src->pub.init_source = init_source;
  src->pub.fill_input_buffer = fill_input_buffer;
  src->pub.skip_input_data = skip_input_data;
  src->pub.resync_to_restart = jpeg_resync_to_restart; /* use default method */
  src->pub.term_source = term_source;
  src->in_buffer = in_buffer;
  src->in_buffer_size = in_buffer_size;
}
