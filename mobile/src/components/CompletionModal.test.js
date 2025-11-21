import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Alert } from 'react-native';
import CompletionModal from './CompletionModal';

// Mock Alert
jest.spyOn(Alert, 'alert');

describe('CompletionModal', () => {
  const mockOnClose = jest.fn();
  const mockOnSubmit = jest.fn();
  const mockTaskTitle = 'Fix HVAC System';

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Rendering', () => {
    it('should render modal when visible', () => {
      const { getAllByText, getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      expect(getAllByText('Complete Task').length).toBeGreaterThan(0);
      expect(getByText(mockTaskTitle)).toBeTruthy();
      expect(getByText(/Work Summary/)).toBeTruthy();
    });

    it('should not render modal when not visible', () => {
      const { queryByText } = render(
        <CompletionModal
          visible={false}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      expect(queryByText('Complete Task')).toBeNull();
    });

    it('should render required field marker', () => {
      const { getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      expect(getByText('*')).toBeTruthy();
    });

    it('should render photo upload placeholder', () => {
      const { getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      expect(getByText('Photo (Optional)')).toBeTruthy();
      expect(getByText('📷 Photo upload coming soon')).toBeTruthy();
    });

    it('should render action buttons', () => {
      const { getByText, getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      expect(getByText('Cancel')).toBeTruthy();
      const completeButtons = getAllByText('Complete Task');
      expect(completeButtons.length).toBeGreaterThan(0);
    });
  });

  describe('Text Input', () => {
    it('should update work summary when text is entered', () => {
      const { getByPlaceholderText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the HVAC system');

      expect(textInput.props.value).toBe('Fixed the HVAC system');
    });

    it('should allow multiline text input', () => {
      const { getByPlaceholderText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      expect(textInput.props.multiline).toBe(true);
    });
  });

  describe('Form Submission', () => {
    it('should call onSubmit with work summary when form is valid', async () => {
      mockOnSubmit.mockResolvedValueOnce();

      const { getByPlaceholderText, getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the HVAC system');

      const submitButton = getAllByText('Complete Task')[1]; // Get the button, not the header
      fireEvent.press(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalledWith('Fixed the HVAC system');
      });
    });

    it('should show alert when work summary is empty', async () => {
      const { getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      await waitFor(() => {
        expect(Alert.alert).toHaveBeenCalledWith(
          'Required Field',
          'Please enter a work summary'
        );
        expect(mockOnSubmit).not.toHaveBeenCalled();
      });
    });

    it('should show alert when work summary is only whitespace', async () => {
      const { getByPlaceholderText, getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, '   ');

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      await waitFor(() => {
        expect(Alert.alert).toHaveBeenCalledWith(
          'Required Field',
          'Please enter a work summary'
        );
        expect(mockOnSubmit).not.toHaveBeenCalled();
      });
    });

    it('should disable buttons while submitting', async () => {
      let resolveSubmit;
      mockOnSubmit.mockImplementation(() => new Promise(resolve => {
        resolveSubmit = resolve;
      }));

      const { getByPlaceholderText, getAllByText, getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the system');

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      // Check that button is disabled immediately after press
      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });

      // The close button should also be disabled
      const closeButton = getByText('✕');
      fireEvent.press(closeButton);
      expect(mockOnClose).not.toHaveBeenCalled();

      // Resolve the promise to clean up
      resolveSubmit();
    });

    it('should clear work summary after successful submission', async () => {
      mockOnSubmit.mockResolvedValueOnce();

      const { getByPlaceholderText, getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the system');

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
        expect(textInput.props.value).toBe('');
      });
    });
  });

  describe('Modal Close', () => {
    it('should call onClose when close button is pressed', () => {
      const { getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const closeButton = getByText('✕');
      fireEvent.press(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should call onClose when cancel button is pressed', () => {
      const { getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const cancelButton = getByText('Cancel');
      fireEvent.press(cancelButton);

      expect(mockOnClose).toHaveBeenCalled();
    });

    it('should clear work summary when modal is closed', () => {
      const { getByPlaceholderText, getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Some text');

      const closeButton = getByText('✕');
      fireEvent.press(closeButton);

      expect(mockOnClose).toHaveBeenCalled();
      expect(textInput.props.value).toBe('');
    });

    it('should not close modal while submitting', async () => {
      mockOnSubmit.mockImplementation(() => new Promise(resolve => setTimeout(resolve, 100)));

      const { getByPlaceholderText, getAllByText, getByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the system');

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      const closeButton = getByText('✕');
      fireEvent.press(closeButton);

      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });

  describe('Error Handling', () => {
    it('should handle submission error gracefully', async () => {
      mockOnSubmit.mockRejectedValueOnce(new Error('Network error'));

      const { getByPlaceholderText, getAllByText } = render(
        <CompletionModal
          visible={true}
          onClose={mockOnClose}
          onSubmit={mockOnSubmit}
          taskTitle={mockTaskTitle}
        />
      );

      const textInput = getByPlaceholderText('Describe the work completed...');
      fireEvent.changeText(textInput, 'Fixed the system');

      const submitButton = getAllByText('Complete Task')[1];
      fireEvent.press(submitButton);

      await waitFor(() => {
        expect(mockOnSubmit).toHaveBeenCalled();
      });

      // After error, modal should still be visible (not close automatically)
      expect(mockOnClose).not.toHaveBeenCalled();
    });
  });
});
