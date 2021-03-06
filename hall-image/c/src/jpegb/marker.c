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

      FILE:    MARKER.C
      AUTHORS: Michael Garris
               Craig Watson
      DATE:    01/16/2001
      UPDATED: 03/16/2005 by MDG

      Contains routines responsible for processing Baseline (lossy)
      JPEG markers.

      ROUTINES:
#cat: read_marker_jpegb - Reads a specified JPEG marker from
#cat:                     an open file.
#cat: getc_marker_jpegb - Reads a specified JPEG marker from a
#cat:                     memory buffer.
#cat: put_nistcom_jpegb - Inserts a NISTCOM comment into a JPEGB datastream.
#cat: read_nistcom_jpegb - Reads the first NISTCOM comment from an
#cat:                     open JPEGB file.
#cat: getc_nistcom_jpegb - Reads the first NISTCOM comment from a
#cat:                     memory buffer containing a JPEGB datastream.

***********************************************************************/

#include <stdio.h>
#include <string.h>
#include "../include/jpegb.h"
#include "../include/jpegl.h"
#include "../include/computil.h"
#include "../include/swap.h"
#include "../include/dataio.h"

/*****************************************/
/* Get markers from compressed open file */
/*****************************************/
int read_marker_jpegb(unsigned short *omarker, const int type, FILE *infp)
{
   int ret;
   unsigned short marker;

   if((ret = read_ushort(&marker, infp)))
      return(ret);

   switch(type){
   case SOI:
      if(marker != SOI) {
         fprintf(stderr,
         "ERROR : read_marker_jpegb : No SOI marker. {%d}\n", marker);
         return(-2);
      }
      break;
   case ANY:
      if((marker & 0xff00) != 0xff00){
	fprintf(stderr,"ERROR : read_marker_jpegb : no marker found {%04X}\n",
                marker);
         return(-3);
      }
      break;
   default:
      fprintf(stderr,
      "ERROR : read_marker_jpegb : invalid marker case -> {%4X}\n", type);
      return(-4);
   }

   *omarker =  marker;
   return(0);
}

/*********************************************/
/* Get markers from compressed memory buffer */
/*********************************************/
int getc_marker_jpegb(unsigned short *omarker, const int type,
                      unsigned char **cbufptr, unsigned char *ebufptr)
{
   int ret;
   unsigned short marker;

   if((ret = getc_ushort(&marker, cbufptr, ebufptr)))
      return(ret);

   switch(type){
   case SOI:
      if(marker != SOI) {
         fprintf(stderr,
         "ERROR : getc_marker_jpegb : No SOI marker. {%d}\n", marker);
         return(-2);
      }
      break;
   case ANY:
      if((marker & 0xff00) != 0xff00){
	fprintf(stderr,"ERROR : getc_marker_jpegb : no marker found {%04X}\n",
                marker);
         return(-3);
      }
      break;
   default:
      fprintf(stderr,
      "ERROR : getc_marker_jpegb : invalid marker case -> {%4X}\n", type);
      return(-4);
   }

   *omarker =  marker;
   return(0);
}

/*******************************************/
int put_nistcom_jpegb(j_compress_ptr cinfo, char *comment_text,
                       const int w, const int h, const int d, const int ppi,
                       const int lossyflag, const int quality)
{
   int ret, gencomflag;
   NISTCOM *nistcom;
   char *comstr, *colorspace;
   int n_cmpnts;

   /* Add Comment(s) here. */
   nistcom = (NISTCOM *)NULL;
   gencomflag = 0;
   if(comment_text != (char *)NULL){
      /* if NISTCOM ... */
      if(strncmp(comment_text, NCM_HEADER, strlen(NCM_HEADER)) == 0){
         if((ret = string2fet(&nistcom, comment_text))){
            return(ret);
         }
      }
      /* If general comment ... */
      else{
         gencomflag = 1;
      }
   }
   /* Otherwise, no comment passed ... */

   n_cmpnts = cinfo->input_components;
   if(n_cmpnts == 1)
      colorspace = (char *)"GRAY";
   else if(n_cmpnts == 3)
      colorspace = (char *)"YCbCr";
   else{
      fprintf(stderr, "ERROR : put_nistcom_jpegb : \n");
      fprintf(stderr, "number of components = %d != {1,3}\n", n_cmpnts);
      if(nistcom != (NISTCOM *)NULL)
         freefet(nistcom);
      return(-2);
   }

   /* Combine image attributes to NISTCOM. */
   if((ret = combine_jpegb_nistcom(&nistcom, w, h, d, ppi, lossyflag,
                          colorspace, n_cmpnts, 1 /* intrlv */, quality))){
      if(nistcom != (NISTCOM *)NULL)
         freefet(nistcom);
      return(ret);
   }

   /* Put NISTCOM ... */
   /* NISTCOM to string. */
   if((ret = fet2string(&comstr, nistcom))){
      freefet(nistcom);
      return(ret);
   }
   /* Put NISTCOM comment string. */
   jpeg_write_marker(cinfo, JPEG_COM, (JOCTET *)comstr, (int)strlen(comstr));
   freefet(nistcom);
   free(comstr);

   /* If general comment exists ... */
   if(gencomflag){
      /* Put general comment to its own segment. */
      jpeg_write_marker(cinfo, JPEG_COM, (JOCTET *)comment_text,
                        (int)strlen(comment_text));
   }

   return(0);
}

/***************************************************************/
/* Get and return first NISTCOM from encoded open file.        */
/***************************************************************/
int read_nistcom_jpegb(NISTCOM **onistcom, FILE *infp)
{
   int ret;
   long savepos;
   unsigned short marker;
   NISTCOM *nistcom;
   char *value, *comment_text;
   unsigned char *ucomment_text;
   int id_len;

   /* Get SOI */
   if((ret = read_marker_jpegb(&marker, SOI, infp)))
      return(ret);

   /* Get next marker. */
   if((ret = read_marker_jpegb(&marker, ANY, infp)))
      return(ret);

   /* Allocate temporary buffer the size of the NIST_COM Header ID. */
   id_len = (int)strlen(NCM_HEADER);
   value = (char *)calloc(id_len+1, sizeof(char));
   if(value == (char *)NULL){
      fprintf(stderr, "ERROR : read_nistcom_jpegb : calloc : value\n");
      return(-2);
   }

   /* While not at Start of Scan (SOS) -     */
   /*    the start of encoded image data ... */
   while(marker != SOS){
      if(marker == COM){
         if((savepos = ftell(infp)) < 0){
            fprintf(stderr, "ERROR : read_nistcom_jpegb : ");
            fprintf(stderr, "ftell : unable to determine current position\n");
            free(value);
            return(-3);
         }
         /* Skip Length (short) Bytes */
         if(fseek(infp, 2L, SEEK_CUR) < 0){
            fprintf(stderr, "ERROR : read_nistcom_jpegb : ");
            fprintf(stderr, "fseek : unable to skip length bytes\n");
            free(value);
            return(-4);
         }
         /* Should be a safe assumption here that we can read */
         /* id_len bytes without reaching EOF, so if we don't */
         /* read all id_len bytes, then flag error.           */
         ret = (int)fread(value, sizeof(char), id_len, infp);
         if(ret != id_len){
            fprintf(stderr, "ERROR : read_nistcom_jpegb : ");
            fprintf(stderr, "fread : only %d of %d bytes read\n",
                    ret, id_len);
            free(value);
            return(-5);
         }
         /* Reset file pointer to original position. */
         if(fseek(infp, savepos, SEEK_SET) < 0){
            fprintf(stderr, "ERROR : read_nistcom_jpegb : ");
            fprintf(stderr, "fseek : unable to reset file position\n");
            free(value);
            return(-6);
         }
         if(strncmp(value, NCM_HEADER, id_len) == 0){
            if((ret = read_comment(&ucomment_text, infp))){
               free(value);
               return(ret);
            }
            comment_text = (char *)ucomment_text;
            if((ret = string2fet(&nistcom, comment_text))){
               free(value);
               return(ret);
            }
            free(value);
            *onistcom = nistcom;
            return(0);
         }
      }
      /* Skip marker segment. */
      if((ret = read_skip_marker_segment(marker, infp))){
         free(value);
         return(ret);
      }
      /* Get next marker. */
      if((ret = read_marker_jpegb(&marker, ANY, infp))){
         free(value);
         return(ret);
      }
   }

   free(value);

   /* NISTCOM not found ... */
   *onistcom = (NISTCOM *)NULL;
   return(0);
}

/*****************************************************************/
/* Get and return first NISTCOM from encoded data stream.        */
/*****************************************************************/
int getc_nistcom_jpegb(NISTCOM **onistcom, unsigned char *idata,
                        const int ilen)
{
   int ret;
   unsigned short marker;
   unsigned char *cbufptr, *ebufptr;
   NISTCOM *nistcom;
   char *comment_text;
   unsigned char *ucomment_text;

   cbufptr = idata;
   ebufptr = idata + ilen;

   /* Get SOI */
   if((ret = getc_marker_jpegb(&marker, SOI, &cbufptr, ebufptr)))
      return(ret);

   /* Get next marker. */
   if((ret = getc_marker_jpegb(&marker, ANY, &cbufptr, ebufptr)))
      return(ret);

   /* While not at Start of Scan (SOS) -     */
   /*    the start of encoded image data ... */
   while(marker != SOS){
      if(marker == COM){
         if(strncmp((char *)cbufptr+2 /* skip Length */,
                    NCM_HEADER, strlen(NCM_HEADER)) == 0){
            if((ret = getc_comment(&ucomment_text, &cbufptr, ebufptr)))
               return(ret);
            comment_text = (char *)ucomment_text;
            if((ret = string2fet(&nistcom, comment_text)))
               return(ret);
            *onistcom = nistcom;
            return(0);
         }
      }
      /* Skip marker segment. */
      if((ret = getc_skip_marker_segment(marker, &cbufptr, ebufptr)))
         return(ret);
      /* Get next marker. */
      if((ret = getc_marker_jpegb(&marker, ANY, &cbufptr, ebufptr)))
         return(ret);
   }

   /* NISTCOM not found ... */
   *onistcom = (NISTCOM *)NULL;
   return(0);
}
