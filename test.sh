#!/bin/bash

ant
echo "mlogo interpreter tests..."

# I need to loop over the files in the test directory,
# execute each .logo file

for f in test/*.logo
do
	filename=${f##*/}
	base=${f%%.*}

	rm -f test/tmpout.txt
	java -jar dist/MLogo.jar $f > test/tmpout.txt

	if [ "$?" -ne "0" ]; then

		if [ -e $base.err ]; then
			# if the program executes unsuccessfully,
			# ensure that it printed the correct error message:

			cmp -s test/tmpout.txt $base.err
			
			if [ "$?" -ne "0" ]; then
				echo "error mismatch in test $filename!"
				echo "expected: "
				awk '{print "\t", $0}' < $base.err
				echo ""
				echo "observed: "
				awk '{print "\t", $0}' < test/tmpout.txt
				echo ""
				exit 1
			fi
		else
			echo "test $filename failed unexpectedly:"
			awk '{print "\t", $0}' < test/tmpout.txt
			echo ""
			exit 1
		fi
	else
		# if the program executes successfully,
		# compare its output to a reference .out file:

		cmp -s test/tmpout.txt $base.out

		if [ "$?" -ne "0" ]; then
			echo "output mismatch in test $filename!"
			echo "expected: "
			awk '{print "\t", $0}' < $base.out
			echo ""
			echo "observed: "
			awk '{print "\t", $0}' < test/tmpout.txt
			echo ""
			exit 1
		fi
	fi
done

rm -f test/tmpout.txt
echo "all tests successful!"