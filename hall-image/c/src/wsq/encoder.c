/*******************************************************************************

License: 
This software was developed at the National Institute of Standards and 
Technology (NIST) by employees of the Federal Government in the course 
of their official duties. Pursuant to title 17 Section 105 of the 
United States Code, this software is not subject to copyright protection 
and is in the public domain. NIST assumes no responsibility  whatsoever for 
its use by other parties, and makes no guarantees, expressed or implied, 
about its quality, reliability, or any other characteristic. 

This software has been determined to be outside the scope of the EAR
(see Part 734.3 of the EAR for exact details) as it has been created solely
by employees of the U.S. Government; it is freely distributed with no
licensing requirements; and it is considered public domain.  Therefore,
it is permissible to distribute this software as a free download from the
internet.

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
      LIBRARY: WSQ - Grayscale Image Compression

      FILE:    ENCODER.C
      AUTHORS: Craig Watson
               Michael Garris
      DATE:    11/24/1999
      UPDATED: 04/25/2005 by MDG

      Contains routines responsible for WSQ encoding image
      pixel data.

      ROUTINES:
#cat: wsq_encode_mem - WSQ encodes image data storing the compressed
#cat:                   bytes to a memory buffer.
#cat: gen_hufftable_wsq - Generates a huffman table for a quantized
#cat:                   data block.
#cat: compress_block - Codes a quantized image using huffman tables.
#cat:
#cat: count_block - Counts the number of occurrences of each category
#cat:                   in a huffman table.

***********************************************************************/

#include <string.h>
#include "../include/wsq.h"
#include "../include/dataio.h"

/************************************************************************/
/*              This is an implementation based on the Crinimal         */
/*              Justice Information Services (CJIS) document            */
/*              "WSQ Gray-scale Fingerprint Compression                 */
/*              Specification", Dec. 1997.                              */
/************************************************************************/
/* WSQ encodes/compresses an image pixmap.                              */
/************************************************************************/
int wsq_encode_mem(unsigned char **odata, int *olen, const float r_bitrate,
                   unsigned char *idata, const int w, const int h,
                   const int d, const int ppi, char *comment_text)
{
   int ret, num_pix;
   float *fdata;                 /* floating point pixel image  */
   float m_shift, r_scale;       /* shift/scale parameters      */
   short *qdata;                 /* quantized image pointer     */
   int qsize, qsize1, qsize2, qsize3;  /* quantized block sizes */
   unsigned char *huffbits, *huffvalues; /* huffman code parameters     */
   HUFFCODE *hufftable;          /* huffcode table              */
   unsigned char *huff_buf;      /* huffman encoded buffer      */
   int hsize, hsize1, hsize2, hsize3; /* Huffman coded blocks sizes */
   unsigned char *wsq_data;      /* compressed data buffer      */
   int wsq_alloc, wsq_len;       /* number of bytes in buffer   */
   int block_sizes[2];

   W_TREE w_tree[W_TREELEN];

   Q_TREE q_tree[Q_TREELEN];
   QUANT_VALS quant_vals;

#ifdef FILTBANK_EVEN_8X8_1
   float hifilt[MAX_HIFILT] =  {
	   0.03226944131446922,
	   -0.05261415011924844,
	   -0.18870142780632693,
	   0.60328894481393847,
	   -0.60328894481393847,
	   0.18870142780632693,
	   0.05261415011924844,
	   -0.03226944131446922 };

	   float lofilt[MAX_LOFILT] =  {
		   0.07565691101399093,
		   -0.12335584105275092,
		   -0.09789296778409587,
		   0.85269867900940344,
		   0.85269867900940344,
		   -0.09789296778409587,
		   -0.12335584105275092,
		   0.07565691101399093 };
#else
   float hifilt[MAX_HIFILT] =  { 0.06453888262893845f,
	   -0.04068941760955844f,
	   -0.41809227322221221f,
	   0.78848561640566439f,
	   -0.41809227322221221f,
	   -0.04068941760955844f,
	   0.06453888262893845f };

   float lofilt[MAX_LOFILT] =  { 0.03782845550699546f,
	   -0.02384946501938000f,
	   -0.11062440441842342f,
	   0.37740285561265380f,
	   0.85269867900940344f,
	   0.37740285561265380f,
	   -0.11062440441842342f,
	   -0.02384946501938000f,
	   0.03782845550699546f };
#endif

   memset(w_tree,0,sizeof(w_tree));
   memset(q_tree,0,sizeof(q_tree));

   /* Compute the total number of pixels in image. */
   num_pix = w * h;

   /* Allocate floating point pixmap. */
   if((fdata = (float *) malloc(num_pix*sizeof(float))) == NULL) {
      //fprintf(stderr,"ERROR : wsq_encode_1 : malloc : fdata\n");
      return(-10);
   }

   /* Convert image pixels to floating point. */
   conv_img_2_flt(fdata, &m_shift, &r_scale, idata, num_pix);

   //if(debug > 0)
   //   fprintf(stderr, "Input image pixels converted to floating point\n\n");

   /* Build WSQ decomposition trees */
   build_wsq_trees(w_tree, W_TREELEN, q_tree, Q_TREELEN, w, h);

   //if(debug > 0)
   //   fprintf(stderr, "Tables for wavelet decomposition finished\n\n");

   /* WSQ decompose the image */
   if((ret = wsq_decompose(fdata, w, h, w_tree, W_TREELEN,
                            hifilt, MAX_HIFILT, lofilt, MAX_LOFILT))){
      free(fdata);
      return(ret);
   }

   //if(debug > 0)
   //   fprintf(stderr, "WSQ decomposition of image finished\n\n");

   /* Set compression ratio and 'q' to zero. */
   quant_vals.cr = 0;
   quant_vals.q = 0.0;
   /* Assign specified r-bitrate into quantization structure. */
   quant_vals.r = r_bitrate;
   /* Compute subband variances. */
   variance(&quant_vals, q_tree, Q_TREELEN, fdata, w, h);

   //if(debug > 0)
   //   fprintf(stderr, "Subband variances computed\n\n");

   /* Quantize the floating point pixmap. */
   if((ret = quantize(&qdata, &qsize, &quant_vals, q_tree, Q_TREELEN,
                      fdata, w, h))){
      free(fdata);
      return(ret);
   }

   /* Done with floating point wsq subband data. */
   free(fdata);

   //if(debug > 0)
   //   fprintf(stderr, "WSQ subband decomposition data quantized\n\n");

   /* Compute quantized WSQ subband block sizes */
   quant_block_sizes(&qsize1, &qsize2, &qsize3, &quant_vals,
                           w_tree, W_TREELEN, q_tree, Q_TREELEN);

   if(qsize != qsize1+qsize2+qsize3){
      //fprintf(stderr,
      //        "ERROR : wsq_encode_1 : problem w/quantization block sizes\n");
      return(-11);
   }

   /* Allocate a WSQ-encoded output buffer.  Allocate this buffer */
   /* to be the size of the original pixmap.  If the encoded data */
   /* exceeds this buffer size, then throw an error because we do */
   /* not want our compressed data to be larger than the original */
   /* image data.                                                 */
   wsq_data = (unsigned char *)malloc(num_pix);
   if(wsq_data == (unsigned char *)NULL){
      free(qdata);
      //fprintf(stderr, "ERROR : wsq_encode_1 : malloc : wsq_data\n");
      return(-12);
   }
   wsq_alloc = num_pix;
   wsq_len = 0;

   /* Add a Start Of Image (SOI) marker to the WSQ buffer. */
   if((ret = putc_ushort(SOI_WSQ, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      return(ret);
   }

   if((ret = putc_nistcom_wsq(comment_text, w, h, d, ppi, 1 /* lossy */,
                             r_bitrate, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      return(ret);
   }

   /* Store the Wavelet filter taps to the WSQ buffer. */
   if((ret = putc_transform_table(lofilt, MAX_LOFILT,
                                 hifilt, MAX_HIFILT,
                                 wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      return(ret);
   }

   /* Store the quantization parameters to the WSQ buffer. */
   if((ret = putc_quantization_table(&quant_vals,
                                    wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      return(ret);
   }

   /* Store a frame header to the WSQ buffer. */
   if((ret = putc_frame_header_wsq(w, h, m_shift, r_scale,
                              wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      return(ret);
   }

   //if(debug > 0)
   //   fprintf(stderr, "SOI, tables, and frame header written\n\n");

   /* Allocate a temporary buffer for holding compressed block data.    */
   /* This buffer is allocated to the size of the original input image, */
   /* and it is "assumed" that the compressed blocks will not exceed    */
   /* this buffer size.                                                 */
   huff_buf = (unsigned char *)malloc(num_pix);
   if(huff_buf == (unsigned char *)NULL) {
      free(qdata);
      free(wsq_data);
      //fprintf(stderr, "ERROR : wsq_encode_1 : malloc : huff_buf\n");
      return(-13);
   }

   /******************/
   /* ENCODE Block 1 */
   /******************/
   /* Compute Huffman table for Block 1. */
   if((ret = gen_hufftable_wsq(&hufftable, &huffbits, &huffvalues,
                              qdata, &qsize1, 1))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   /* Store Huffman table for Block 1 to WSQ buffer. */
   if((ret = putc_huffman_table(DHT_WSQ, 0, huffbits, huffvalues,
                               wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(huffbits);
      free(huffvalues);
      free(hufftable);
      return(ret);
   }
   free(huffbits);
   free(huffvalues);

   //if(debug > 0)
   //   fprintf(stderr, "Huffman code Table 1 generated and written\n\n");

   /* Compress Block 1 data. */
   if((ret = compress_block(huff_buf, &hsize1, qdata, qsize1,
                           MAX_HUFFCOEFF, MAX_HUFFZRUN, hufftable))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(hufftable);
      return(ret);
   }
   /* Done with current Huffman table. */
   free(hufftable);

   /* Accumulate number of bytes compressed. */
   hsize = hsize1;

   /* Store Block 1's header to WSQ buffer. */
   if((ret = putc_block_header(0, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   /* Store Block 1's compressed data to WSQ buffer. */
   if((ret = putc_bytes(huff_buf, hsize1, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   //if(debug > 0)
   //   fprintf(stderr, "Block 1 compressed and written\n\n");

   /******************/
   /* ENCODE Block 2 */
   /******************/
   /* Compute  Huffman table for Blocks 2 & 3. */
   block_sizes[0] = qsize2;
   block_sizes[1] = qsize3;
   if((ret = gen_hufftable_wsq(&hufftable, &huffbits, &huffvalues,
                          qdata+qsize1, block_sizes, 2))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   /* Store Huffman table for Blocks 2 & 3 to WSQ buffer. */
   if((ret = putc_huffman_table(DHT_WSQ, 1, huffbits, huffvalues,
                               wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(huffbits);
      free(huffvalues);
      free(hufftable);
      return(ret);
   }
   free(huffbits);
   free(huffvalues);

   //if(debug > 0)
   //   fprintf(stderr, "Huffman code Table 2 generated and written\n\n");

   /* Compress Block 2 data. */
   if((ret = compress_block(huff_buf, &hsize2, qdata+qsize1, qsize2,
                           MAX_HUFFCOEFF, MAX_HUFFZRUN, hufftable))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(hufftable);
      return(ret);
   }

   /* Accumulate number of bytes compressed. */
   hsize += hsize2;

   /* Store Block 2's header to WSQ buffer. */
   if((ret = putc_block_header(1, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(hufftable);
      return(ret);
   }

   /* Store Block 2's compressed data to WSQ buffer. */
   if((ret = putc_bytes(huff_buf, hsize2, wsq_data, wsq_alloc, &wsq_len))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(hufftable);
      return(ret);
   }

   //if(debug > 0)
   //   fprintf(stderr, "Block 2 compressed and written\n\n");

   /******************/
   /* ENCODE Block 3 */
   /******************/
   /* Compress Block 3 data. */
   if((ret = compress_block(huff_buf, &hsize3, qdata+qsize1+qsize2, qsize3,
                           MAX_HUFFCOEFF, MAX_HUFFZRUN, hufftable))){
      free(qdata);
      free(wsq_data);
      free(huff_buf);
      free(hufftable);
      return(ret);
   }
   /* Done with current Huffman table. */
   free(hufftable);

   /* Done with quantized image buffer. */
   free(qdata);

   /* Accumulate number of bytes compressed. */
   hsize += hsize3;

   /* Store Block 3's header to WSQ buffer. */
   if((ret = putc_block_header(1, wsq_data, wsq_alloc, &wsq_len))){
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   /* Store Block 3's compressed data to WSQ buffer. */
   if((ret = putc_bytes(huff_buf, hsize3, wsq_data, wsq_alloc, &wsq_len))){
      free(wsq_data);
      free(huff_buf);
      return(ret);
   }

   //if(debug > 0)
   //   fprintf(stderr, "Block 3 compressed and written\n\n");

   /* Done with huffman compressing blocks, so done with buffer. */
   free(huff_buf);

   /* Add a End Of Image (EOI) marker to the WSQ buffer. */
   if((ret = putc_ushort(EOI_WSQ, wsq_data, wsq_alloc, &wsq_len))){
      free(wsq_data);
      return(ret);
   }

   //if(debug >= 1) {
   //   fprintf(stderr,
   //           "hsize1 = %d :: hsize2 = %d :: hsize3 = %d\n", hsize1, hsize2, hsize3);
   //   fprintf(stderr,"@ r = %.3f :: complen = %d :: ratio = %.1f\n",
   //           r_bitrate, hsize, (float)(num_pix)/(float)hsize);
   //}

   *odata = wsq_data;
   *olen = wsq_len;

   /* Return normally. */
   return(0);
}

/*************************************************************/
/* Generate a Huffman code table for a quantized data block. */
/*************************************************************/
int gen_hufftable_wsq(HUFFCODE **ohufftable, unsigned char **ohuffbits,
               unsigned char **ohuffvalues, short *sip, const int *block_sizes,
               const int num_sizes)
{
   int i, j;
   int ret;
   int adjust;          /* tells if codesize is greater than MAX_HUFFBITS */
   int *codesize;       /* code sizes to use */
   int last_size;       /* last huffvalue */
   unsigned char *huffbits;     /* huffbits values */
   unsigned char *huffvalues;   /* huffvalues */
   int *huffcounts;     /* counts for each huffman category */
   int *huffcounts2;    /* counts for each huffman category */
   HUFFCODE *hufftable1, *hufftable2;  /* hufftables */

   if((ret = count_block(&huffcounts, MAX_HUFFCOUNTS_WSQ,
			 sip, block_sizes[0], MAX_HUFFCOEFF, MAX_HUFFZRUN)))
      return(ret);

   for(i = 1; i < num_sizes; i++) {
      if((ret = count_block(&huffcounts2, MAX_HUFFCOUNTS_WSQ,
                           sip+block_sizes[i-1], block_sizes[i],
                           MAX_HUFFCOEFF, MAX_HUFFZRUN)))
         return(ret);

      for(j = 0; j < MAX_HUFFCOUNTS_WSQ; j++)
         huffcounts[j] += huffcounts2[j];

      free(huffcounts2);
   }

   if((ret = find_huff_sizes(&codesize, huffcounts, MAX_HUFFCOUNTS_WSQ))){
      free(huffcounts);
      return(ret);
   }
   free(huffcounts);

   if((ret = find_num_huff_sizes(&huffbits, &adjust, codesize,
                                MAX_HUFFCOUNTS_WSQ))){
      free(codesize);
      return(ret);
   }

   if(adjust){
      if((ret = sort_huffbits(huffbits))){
         free(codesize);
         free(huffbits);
         return(ret);
      }
   }

   if((ret = sort_code_sizes(&huffvalues, codesize, MAX_HUFFCOUNTS_WSQ))){
      free(codesize);
      free(huffbits);
      return(ret);
   }
   free(codesize);

   if((ret = build_huffsizes(&hufftable1, &last_size,
                              huffbits, MAX_HUFFCOUNTS_WSQ))){
      free(huffbits);
      free(huffvalues);
      return(ret);
   }

   build_huffcodes(hufftable1);
   if((ret = check_huffcodes_wsq(hufftable1, last_size))){
      fprintf(stderr, "ERROR: This huffcode warning is an error ");
      fprintf(stderr, "for the encoder.\n");
      free(huffbits);
      free(huffvalues);
      free(hufftable1);
      return(ret);
   }

   if((ret = build_huffcode_table(&hufftable2, hufftable1, last_size,
                                 huffvalues, MAX_HUFFCOUNTS_WSQ))){
      free(huffbits);
      free(huffvalues);
      free(hufftable1);
      return(ret);
   }

   free(hufftable1);

   *ohuffbits = huffbits;
   *ohuffvalues = huffvalues;
   *ohufftable = hufftable2;

   return(0);
}

/*****************************************************************/
/* Routine "codes" the quantized image using the huffman tables. */
/*****************************************************************/
int compress_block(
   unsigned char *outbuf,       /* compressed output buffer            */
   int   *obytes,       /* number of compressed bytes          */
   short *sip,          /* quantized image                     */
   const int sip_siz,   /* size of quantized image to compress */
   const int MaxCoeff,  /* Maximum values for coefficients     */
   const int MaxZRun,   /* Maximum zero runs                   */
   HUFFCODE *codes)     /* huffman code table                  */
{
   unsigned char *optr;
   int LoMaxCoeff;        /* lower (negative) MaxCoeff limit */
   short pix;             /* temp pixel pointer */
   unsigned int rcnt = 0, state;  /* zero run count and if current pixel
                             is in a zero run or just a coefficient */
   int cnt;               /* pixel counter */
   int outbit, bytes;     /* parameters used by write_bits to */
   unsigned char bits;            /* output the "coded" image to the  */
                          /* output buffer                    */

   LoMaxCoeff = 1 - MaxCoeff;
   optr = outbuf;
   outbit = 7;
   bytes = 0;
   bits = 0;
   state = COEFF_CODE;
   for (cnt = 0; cnt < sip_siz; cnt++) {
      pix = *(sip + cnt);

      switch (state) {

         case COEFF_CODE:
            if (pix == 0) {
               state = RUN_CODE;
               rcnt = 1;
               break;
            }
            if (pix > MaxCoeff) { 
               if (pix > 255) {
                  /* 16bit pos esc */
                  write_bits( &optr, (unsigned short) codes[103].code,
                              codes[103].size, &outbit, &bits, &bytes );
                  write_bits( &optr, (unsigned short) pix, 16,
                              &outbit, &bits, &bytes);
               }
               else {
                  /* 8bit pos esc */
                  write_bits( &optr, (unsigned short) codes[101].code,
                              codes[101].size, &outbit, &bits, &bytes );
                  write_bits( &optr, (unsigned short) pix, 8,
                              &outbit, &bits, &bytes);
               }
            }
            else if (pix < LoMaxCoeff) {
               if (pix < -255) {
                  /* 16bit neg esc */
                  write_bits( &optr, (unsigned short) codes[104].code,
                              codes[104].size, &outbit, &bits, &bytes );
                  write_bits( &optr, (unsigned short) -pix, 16,
                              &outbit, &bits, &bytes);
               }
               else {
                  /* 8bit neg esc */
                  write_bits( &optr, (unsigned short) codes[102].code,
                              codes[102].size, &outbit, &bits, &bytes );
                  write_bits( &optr, (unsigned short) -pix, 8,
                              &outbit, &bits, &bytes);
               }
            }
            else {
               /* within table */
               write_bits( &optr, (unsigned short) codes[pix+180].code,
                           codes[pix+180].size, &outbit, &bits, &bytes);
            }
            break;

         case RUN_CODE:
            if (pix == 0  &&  rcnt < 0xFFFF) {
               ++rcnt;
               break;
            }
            if ((int)rcnt <= MaxZRun) {
               /* log zero run length */
               write_bits( &optr, (unsigned short) codes[rcnt].code,
                           codes[rcnt].size, &outbit, &bits, &bytes );
            }
            else if (rcnt <= 0xFF) {
               /* 8bit zrun esc */
               write_bits( &optr, (unsigned short) codes[105].code,
                           codes[105].size, &outbit, &bits, &bytes );
               write_bits( &optr, (unsigned short) rcnt, 8,
                           &outbit, &bits, &bytes);
            }
            else if (rcnt <= 0xFFFF) {
               /* 16bit zrun esc */
               write_bits( &optr, (unsigned short) codes[106].code,
                           codes[106].size, &outbit, &bits, &bytes );
               write_bits( &optr, (unsigned short) rcnt, 16,
                           &outbit, &bits, &bytes);
            }
            else {
               fprintf(stderr,
                      "ERROR : compress_block : zrun too large.\n");
               return(-47);
            }

            if(pix != 0) {
               if (pix > MaxCoeff) {
                  /** log current pix **/
                  if (pix > 255) {
                     /* 16bit pos esc */
                     write_bits( &optr, (unsigned short) codes[103].code,
                                 codes[103].size, &outbit, &bits, &bytes );
                     write_bits( &optr, (unsigned short) pix, 16,
                                 &outbit, &bits, &bytes);
                  }
                  else {
                     /* 8bit pos esc */
                     write_bits( &optr, (unsigned short) codes[101].code,
                                 codes[101].size, &outbit, &bits, &bytes );
                     write_bits( &optr, (unsigned short) pix, 8,
                                 &outbit, &bits, &bytes);
                  }
               }
               else if (pix < LoMaxCoeff) {
                  if (pix < -255) {
                     /* 16bit neg esc */
                     write_bits( &optr, (unsigned short) codes[104].code,
                                 codes[104].size, &outbit, &bits, &bytes );
                     write_bits( &optr, (unsigned short) -pix, 16,
                                 &outbit, &bits, &bytes);
                  }
                  else {
                     /* 8bit neg esc */
                     write_bits( &optr, (unsigned short) codes[102].code,
                                 codes[102].size, &outbit, &bits, &bytes );
                     write_bits( &optr, (unsigned short) -pix, 8,
                                 &outbit, &bits, &bytes);
                  }
               }
               else {
                  /* within table */
                  write_bits( &optr, (unsigned short) codes[pix+180].code,
                              codes[pix+180].size, &outbit, &bits, &bytes);
               }
               state = COEFF_CODE;
            }
            else {
               rcnt = 1;
               state = RUN_CODE;
            }
            break;
      }
   }
   if (state == RUN_CODE) {
      if ((int)rcnt <= MaxZRun) {
         write_bits( &optr, (unsigned short) codes[rcnt].code,
                     codes[rcnt].size, &outbit, &bits, &bytes );
      }
      else if (rcnt <= 0xFF) {
         write_bits( &optr, (unsigned short) codes[105].code,
                     codes[105].size, &outbit, &bits, &bytes );
         write_bits( &optr, (unsigned short) rcnt, 8,
                     &outbit, &bits, &bytes);
      }
      else if (rcnt <= 0xFFFF) {
         write_bits( &optr, (unsigned short) codes[106].code,
                     codes[106].size, &outbit, &bits, &bytes );
         write_bits( &optr, (unsigned short) rcnt, 16,
                     &outbit, &bits, &bytes);
      }
      else {
         fprintf(stderr, "ERROR : compress_block : zrun2 too large.\n");
         return(-48);
      }
   }

   flush_bits( &optr, &outbit, &bits, &bytes);

   *obytes = bytes;
   return(0);
}

/*****************************************************************/
/* This routine counts the number of occurences of each category */
/* in the huffman coding tables.                                 */
/*****************************************************************/
int count_block(
   int **ocounts,     /* output count for each huffman catetory */
   const int max_huffcounts, /* maximum number of counts */
   short *sip,          /* quantized data */
   const int sip_siz,   /* size of block being compressed */
   const int MaxCoeff,  /* maximum values for coefficients */
   const int MaxZRun)   /* maximum zero runs */
{
   int *counts;         /* count for each huffman category */
   int LoMaxCoeff;        /* lower (negative) MaxCoeff limit */
   short pix;             /* temp pixel pointer */
   unsigned int rcnt = 0, state;  /* zero run count and if current pixel
                             is in a zero run or just a coefficient */
   int cnt;               /* pixel counter */

   /* Ininitalize vector of counts to 0. */
   counts = (int *)calloc(max_huffcounts+1, sizeof(int));
   if(counts == (int *)NULL){
      fprintf(stderr,
      "ERROR : count_block : calloc : counts\n");
      return(-48);
   }
   /* Set last count to 1. */
   counts[max_huffcounts] = 1;

   LoMaxCoeff = 1 - MaxCoeff;
   state = COEFF_CODE;
   for(cnt = 0; cnt < sip_siz; cnt++) {
      pix = *(sip + cnt);
      switch(state) {

         case COEFF_CODE:   /* for runs of zeros */
            if(pix == 0) {
               state = RUN_CODE;
               rcnt = 1;
               break;
            }
            if(pix > MaxCoeff) { 
               if(pix > 255)
                  counts[103]++; /* 16bit pos esc */
               else
                  counts[101]++; /* 8bit pos esc */
            }
            else if (pix < LoMaxCoeff) {
               if(pix < -255)
                  counts[104]++; /* 16bit neg esc */
               else
                  counts[102]++; /* 8bit neg esc */
            }
            else
               counts[pix+180]++; /* within table */
            break;

         case RUN_CODE:  /* get length of zero run */
            if(pix == 0  &&  rcnt < 0xFFFF) {
               ++rcnt;
               break;
            }
               /* limit rcnt to avoid EOF problem in bitio.c */
            if((int)rcnt <= MaxZRun)
               counts[rcnt]++;  /** log zero run length **/
            else if(rcnt <= 0xFF)
               counts[105]++;
            else if(rcnt <= 0xFFFF)
               counts[106]++; /* 16bit zrun esc */
            else {
               fprintf(stderr,
               "ERROR: count_block : Zrun to long in count block.\n");
               return(-49);
            }

            if(pix != 0) {
               if(pix > MaxCoeff) { /** log current pix **/
                  if(pix > 255)
                     counts[103]++; /* 16bit pos esc */
                  else
                     counts[101]++; /* 8bit pos esc */
               }
               else if(pix < LoMaxCoeff) {
                  if(pix < -255)
                     counts[104]++; /* 16bit neg esc */
                  else
                     counts[102]++; /* 8bit neg esc */
               }
               else
                  counts[pix+180]++; /* within table */
               state = COEFF_CODE;
            }
            else {
               rcnt = 1;
               state = RUN_CODE;
            }
            break;
      }
   }
   if(state == RUN_CODE){ /** log zero run length **/
      if((int)rcnt <= MaxZRun)
         counts[rcnt]++;
      else if(rcnt <= 0xFF)
         counts[105]++;
      else if(rcnt <= 0xFFFF)
         counts[106]++; /* 16bit zrun esc */
      else {
         fprintf(stderr,
         "ERROR: count_block : Zrun to long in count block.\n");
         return(-50);
      }
   }

   *ocounts = counts;
   return(0);
}